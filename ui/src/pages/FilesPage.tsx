import React, {useEffect, useState} from 'react'
import axios from "axios";
import {useNavigate, useParams} from "react-router-dom";
import "styles/FilesPage.css"
import "components/toolbox/Toolbox.css"
import ConversionUtils from "utils/ConversionUtils";
import {MediaDirectoryInfo, MediaInfo} from "lib/Api";
import MediaCardGrid from "components/FilesPage/MediaCardGrid";
import Breadcrumbs from "components/files/BreadCrumbs";
import DirectoryCardGrid from "components/FilesPage/DirectoryCardGrid/DirectoryCardGrid";
import {Readme} from "components/files/Readme";
import {FilesList} from "components/FilesPage/FilesList";
import useDragSelect from "lib/react/hooks/useDragSelect";

function FilesPage() {
    let [content, setContent] = useState<MediaDirectoryInfo | null>(null);
    let [stats, setStats] = useState<{ [key: string]: any }>({});
    let [readme, setReadme] = useState<string>("");
    let {"*": filePath = ""} = useParams<string>()
    let [pathSegments, setPathSegments] = useState<string[]>([])
    let navigate = useNavigate();
    let [selectedFiles, setSelectedFiles] = useState<string[]>([])

    useEffect(() => {
        setPathSegments(filePath.split("/").filter(it => !!it))
        loadContent(filePath);
        loadStats(filePath)
        loadReadme(filePath)
    }, [filePath])

    useDragSelect("media-card", "media-card-grid", setSelectedFiles)

    async function loadContent(filePath: string) {
        let response = await axios.get("/api/files/list", {
            params: {
                path: filePath
            }
        });
        setContent(response.data)
    }

    async function loadStats(filePath: string) {
        let response = await axios.get("/api/files/stats", {
            params: {
                path: filePath
            }
        });
        setStats(response.data || {})
    }

    async function loadReadme(filePath: string) {
        let response = await axios.get("/api/files/readme", {
            params: {
                path: filePath
            }
        });
        setReadme(response.data?.content || "")
    }

    function renderStats(stats: { [p: string]: any }) {
        return <>
            <p>Forbidden directories: {stats["forbiddenDirectories"]}</p>
            <p>Total size: {ConversionUtils.getReadableSize(stats["totalSize"])}</p>
        </>
    }

    let imageFiles: MediaInfo[] = content?.files?.filter(it => {
        let type: string = (it.source || it.optimized)!!.type;
        return ["jpg", "png", "jpeg"].includes(type)
    }) || []

    function goToPathSegment(n: number) {
        navigate("/files/" + pathSegments.slice(0, n).join("/"))
    }

    function openMedia(fileName: string) {
        let url = "/api/files/show/?path=" + filePath + "/" + fileName;
        window.open(url, '_blank')?.focus()
    }

    async function initOptimizationForSelected() {
        try {
            let response = await axios.post("/api/files/optimize", {
                basePath: filePath,
                paths: selectedFiles
            });
        } catch (e) {
          alert("Could not init optimization")
        }

    }

    return <div>
        <div className="toolbox flex">
            <Breadcrumbs
                names={["/", ...pathSegments]}
                selectFn={i => goToPathSegment(i)}
            />
            <div className={"file-actions-bar"}>
                <button
                    className={"toolbox_item"}
                    onClick={initOptimizationForSelected}
                    title={"Optimize"}
                >Optimize</button>
                <button className={"toolbox_item"} title={"Delete"}>Delete</button>
            </div>
        </div>
        <Readme readme={readme}/>
        {renderStats(stats)}
        <DirectoryCardGrid
            directories={content?.directories || []}
            path={filePath}
            actionOpen={(dirname) => {}}
        />
        <MediaCardGrid
            imageMedias={imageFiles || []}
            path={filePath}
            selectedItems={selectedFiles}
            actionOpen={(fileName) => openMedia(fileName)}
        />
        <FilesList files={content?.files || []} root={filePath}/>
    </div>
}

export default FilesPage

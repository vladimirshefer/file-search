import React, {useEffect, useState} from 'react'
import {useNavigate, useParams} from "react-router-dom";
import "styles/FilesPage.css"
import "components/toolbox/Toolbox.css"
import ConversionUtils from "utils/ConversionUtils";
import {MediaDirectoryInfo, MediaInfo} from "lib/Api";
import MediaCardGrid from "components/FilesPage/MediaCardGrid";
import Breadcrumbs from "components/files/BreadCrumbs";
import DirectoryCardGrid from "components/FilesPage/DirectoryCardGrid/DirectoryCardGrid";
import {Readme} from "components/files/Readme";
import FilesList from "components/FilesPage/FilesList/FilesList";
import FileApiService from "lib/service/FileApiService";
import DragArea from "components/drag/DragArea";

function FilesPage() {
    let [content, setContent] = useState<MediaDirectoryInfo | null>(null);
    let [stats, setStats] = useState<{ [key: string]: any }>({});
    let [readme, setReadme] = useState<string>("");
    let {"*": filePath = ""} = useParams<string>()
    let [pathSegments, setPathSegments] = useState<string[]>([])
    let navigate = useNavigate();
    let [selectedFiles, setSelectedFiles] = useState<string[]>([])
    let fileApiService = new FileApiService()
    let [isLoading, setIsLoading] = useState<boolean>(true);


    async function init() {
        setIsLoading(true);
        setPathSegments(filePath.split("/").filter(it => !!it))
        try {
            await loadContent(filePath)
            await loadStats(filePath)
            await loadReadme(filePath)
        } catch (e) {
            console.log(e);
        }
        setIsLoading(false);

        return function cleanup() {
            setSelectedFiles([]);
            setContent(null);
            setStats({})
            setReadme("")
        }
    }

    useEffect(() => {
        init();
    }, [filePath])


    async function loadContent(filePath: string) {
        setContent(await fileApiService.loadContent(filePath))
    }

    async function loadStats(filePath: string) {
        setStats(await fileApiService.loadStats(filePath) || {})
    }

    async function loadReadme(filePath: string) {
        setReadme(await fileApiService.loadReadme(filePath) || "")
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

    function openMediaOptimized(fileName: string): void {
        alert(`Going to open ${fileName}`)
        return;
    }

    async function initOptimizationForSelected() {
        try {
            await fileApiService.optimize(filePath, selectedFiles)
        } catch (e) {
            alert("Could not init optimization")
        }
    }

    if (isLoading) return <span>LOADING...</span>

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
        <DragArea
            setSelectedItems={setSelectedFiles}
        >
            <DirectoryCardGrid
                directories={content?.directories || []}
                path={filePath}
                selectedDirectories={selectedFiles}
                actionOpen={(dirname) => null}
            />
            <MediaCardGrid
                imageMedias={imageFiles || []}
                path={filePath}
                selectedItems={selectedFiles}
                actionOpenSource={(fileName) => openMedia(fileName)}
                actionOpenOptimized={(fileName) => openMediaOptimized(fileName)}
            />
            <FilesList
                files={content?.files || []}
                root={filePath}
                filesSelected={selectedFiles}
            />
        </DragArea>
    </div>
}

export default FilesPage

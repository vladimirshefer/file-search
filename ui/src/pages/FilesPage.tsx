import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useNavigate, useParams} from "react-router-dom";
import "styles/FilesPage.css"
import ConversionUtils from "utils/ConversionUtils";
import {MediaDirectoryInfo, MediaInfo} from "lib/Api";
import MediaCardGrid from "components/FilesPage/MediaCardGrid";
import Breadcrumbs from "components/files/BreadCrumbs";
import DirectoryCard from "components/FilesPage/DirectoryCard";
import {Readme} from "components/files/Readme";

function FilesPage() {
    let [content, setContent] = useState<MediaDirectoryInfo | null>(null);
    let [stats, setStats] = useState<{ [key: string]: any }>({});
    let [readme, setReadme] = useState<string>("");
    let {"*": filePath = ""} = useParams<string>()
    let [pathSegments, setPathSegments] = useState<string[]>([])
    let navigate = useNavigate();

    useEffect(() => {
        setPathSegments(filePath.split("/"))
        loadContent(filePath)
        loadStats(filePath)
        loadReadme(filePath)
    }, [filePath])

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

    return <div>
        <Breadcrumbs
            names={["/", ...pathSegments]}
            selectFn={i => goToPathSegment(i)}
        />
        <Readme readme={readme}/>
        {renderStats(stats)}
        {DirectoriesList(content?.directories || [], filePath)}
        <MediaCardGrid imageMedias={imageFiles || []} path={filePath} actionOpen={(fileName) => openMedia(fileName)}/>
        {FilesList(content?.files || [], filePath)}
    </div>
}

function DirectoriesList(directories: MediaDirectoryInfo[], root: string) {
    return <>
        <p>
            Total directories: {directories.length}
        </p>
        <ul className="file-tree_directories-list">
            {directories.map((directory) =>
                <DirectoryCard name={directory.name} parent={root} key={directory.name}/>
            )}
        </ul>
    </>;
}

function FilesList(files: MediaInfo[], root: string) {
    function FileInfo(file: MediaInfo) {
        let filename = file.source?.name || file.optimized?.name;
        return <li key={filename} className={"file-info"}>
            <span className={"file-info_name"}>
                {filename}
            </span>
            <span className={"file-info_name"}>
                {filename}
            </span>
            {(!!file.optimized) ?
                <span className={"file-info_name-optimized"}>
                    file.optimized.name
                </span>
                : null}
            <span className={"file-info_size"}>
                {ConversionUtils.getReadableSize(file.source?.size || null)}
                /
                {ConversionUtils.getReadableSize(file.optimized?.size || null)}
            </span>
            <Link
                to={"/edit/" + root + "/" + filename}
                relative={"route"}
                className={"file-info_button"}
            >
                <button type={"button"}>Edit text</button>
            </Link>
            <a
                href={"/api/files/show/?path=" + root + "/" + filename}
                target={"_blank"}
                className={"file-info_button"}
            >
                <button type={"button"}>Open</button>
            </a>
        </li>;
    }

    return <div className={"file-tree_files-list"}>
        <p className={"files-list_header"}>
            Total files: {files.length}
        </p>
        <ul className="files-list_list">
            {files.map((file) => FileInfo(file))}
        </ul>
    </div>;
}

export default FilesPage

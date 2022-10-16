import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useParams} from "react-router-dom";
import "styles/FilesPage.css"
import ConversionUtils from "utils/ConversionUtils";
import {MediaDirectoryInfo, MediaInfo} from "lib/Api";
import MediaCard from "../components/FilesPage/MediaCard";

function FilesPage() {
    let [content, setContent] = useState<MediaDirectoryInfo | null>(null);
    let [stats, setStats] = useState<{ [key: string]: any }>({});
    let [readme, setReadme] = useState<string>("");
    let {"*": filePath = ""} = useParams<string>()

    useEffect(() => {
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

    function renderImageGallery(imageMedias: MediaInfo[], path: string) {
        return <ul className={"media-cards"}>
            {
                imageMedias.map(it => {
                    let name = (it.source || it.optimized)!!.name;
                    return <MediaCard name={name} path={path} key={name} status={it.status}/>
                })
            }
        </ul>
    }

    return <div>
        <h1>{content?.name || "Files tree"}</h1>
        <span>{content?.path}</span>
        <div className={"readme"}>
            <h3>README</h3>
            <pre className={"readme_text"}>
                {readme}
            </pre>
        </div>
        {renderStats(stats)}
        {DirectoriesList(content?.directories || [], filePath)}
        {renderImageGallery(imageFiles || [], filePath)}
        {FilesList(content?.files || [], filePath)}
    </div>
}

function DirectoriesList(directories: MediaDirectoryInfo[], root: string) {
    function DirectoryInfo(
        {
            name,
            parent
        }: {
            name: string,
            parent: string
        }) {

        let [size, setSize] = useState<number | null>(null)

        async function requestSize() {
            let result = await axios.get("/api/files/size", {
                params: {
                    path: parent + "/" + name
                }
            });

            setSize(result.data?.size)
        }

        return <li key={name} className={"directory-info"}>
            <Link
                to={"./" + name}
                relative={"path"}
                className={"directory-info_name"}
                title={name}
            >
                {name}
            </Link>
            <span
                className={"directory-info_size"}
                onClick={() => requestSize()}
            >
                {ConversionUtils.getReadableSize(size)}
            </span>
        </li>;
    }

    return <>
        <p>
            Total directories: {directories.length}
        </p>
        <ul className="file-tree_directories-list">
            {root !== "" ? (
                <DirectoryInfo name=".." parent={root}/>
            ) : ("")}
            {
                directories.map((directory) =>
                    <DirectoryInfo name={directory.name} parent={root}/>
                )
            }
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

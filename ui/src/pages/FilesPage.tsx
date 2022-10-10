import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useParams} from "react-router-dom";
import "styles/FilesPage.css"
import ConversionUtils from "../utils/ConversionUtils";

interface FileInfoDto {
    name: string
    size: string
}

interface DirectoryInfoDto {
    name: string
}

function FilesPage() {
    let [files, setFiles] = useState<FileInfoDto[]>([]);
    let [directories, setDirectories] = useState<DirectoryInfoDto[]>([]);
    let [stats, setStats] = useState<{ [key: string]: any }>({});
    let [readme, setReadme] = useState<string>("");
    let {"*": filePath = ""} = useParams<string>()

    useEffect(() => {
        loadContent(filePath)
    }, [filePath])

    useEffect(() => {
        loadStats(filePath)
        loadReadme(filePath)
    }, [filePath])

    async function loadContent(filePath: string) {
        let response = await axios.get("/api/files/list", {
            params: {
                path: filePath
            }
        });
        setFiles(response.data.files || [])
        setDirectories(response.data.directories || [])
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

    return <div>
        <h1>Files tree</h1>
        <div className={"readme"}>
            <h3>README</h3>
            <pre className={"readme_text"}>
                {readme}
            </pre>
        </div>
        {renderStats(stats)}
        {DirectoriesList(directories, filePath)}
        {FilesList(files, filePath)}
    </div>
}

function DirectoriesList(directories: DirectoryInfoDto[], root: string) {
    function DirectoryInfo(directory: DirectoryInfoDto) {
        return <li key={directory.name} className={"directory-info"}>
            <Link
                to={"./" + directory.name}
                relative={"path"}
                className={"directory-info_name"}
                title={directory.name}
            >
                {directory.name}
            </Link>
        </li>;
    }

    return <>
        <p>
            Total directories: {directories.length}
        </p>
        <ul className="file-tree_directories-list">
            {root !== "" ? (
                DirectoryInfo({name: ".."} as DirectoryInfoDto)
            ) : ("")}
            {
                directories.map((directory) => DirectoryInfo(directory))
            }
        </ul>
    </>;
}

function FilesList(files: FileInfoDto[], root: string) {
    function FileInfo(file: FileInfoDto) {
        return <li key={file.name} className={"file-info"}>
            <span className={"file-info_name"}>
                {file.name}
            </span>
            <span className={"file-info_size"}>
                {ConversionUtils.getReadableSize(+file.size)}
            </span>
            <Link
                to={"/edit/" + root + "/" + file.name}
                relative={"route"}
                className={"file-info_button"}
            >
                <button type={"button"}>Edit text</button>
            </Link>
            <a
                href={"/api/files/show/?path=" + root + "/" + file.name}
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

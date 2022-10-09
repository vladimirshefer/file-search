import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useParams} from "react-router-dom";
import "styles/FilesPage.css"

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
    let {"*": filePath = ""} = useParams<string>()

    useEffect(() => {
        loadContent(filePath)
    }, [filePath])

    useEffect(() => {
        loadStats(filePath)
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

    function renderDirectories(directories: DirectoryInfoDto[]) {
        return <>
            <p>
                Total directories: {directories.length}
            </p>
            <ul className="file-tree_directories-list">
                {filePath !== "" ? (
                    <li key={".."}>
                        <Link to={".."} relative={"path"}>..</Link>
                    </li>
                ) : ("")}
                {
                    directories.map((directory) =>
                        <li key={directory.name}>
                            <Link to={"./" + directory.name} relative={"path"}> {directory.name}</Link>
                        </li>
                    )
                }
            </ul>
        </>;
    }

    function renderStats(stats: { [p: string]: any }) {
        return <>
            <p>Forbidden directories: {stats["forbiddenDirectories"]}</p>
            <p>Total size: {stats["totalSize"]}</p>
        </>
    }

    return <div>
        <h1>Files tree</h1>
        {renderStats(stats)}
        {renderDirectories(directories)}
        {FilesList(files, filePath)}
    </div>
}

function FilesList(files: FileInfoDto[], root: string) {
    return <>
        <p>
            Total files: {files.length}
        </p>
        <ul className="file-tree_files-list">
            {
                files.map((file) =>
                    <li key={file.name}>
                        <p>{file.name}</p>
                        Size: {file.size}b.
                        <Link to={"/edit/" + root + "/" + file.name} relative={"route"}>
                            <button type={"button"}>Edit text</button>
                        </Link>
                        <a href={"/api/files/show/?path=" + root + "/" + file.name} target={"_blank"}>
                            <button type={"button"}>Open</button>
                        </a>
                    </li>
                )
            }
        </ul>
    </>;
}

export default FilesPage

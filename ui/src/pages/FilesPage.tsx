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
    let {"*": filePath = ""} = useParams<string>()

    useEffect(() => {
        loadContent(filePath)
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

    return <div>
        <h1>Files tree</h1>
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
        <p>
            Total files: {files.length}
        </p>
        <ul className="file-tree_files-list">
            {
                files.map((file) =>
                    <li key={file.name}>
                        {file.name} : {file.size}
                        <Link to={"/edit/" + filePath + "/" + file.name} relative={"route"}>
                            <button type={"button"}>Edit text</button>
                        </Link>
                        <a href={"/api/files/show/?path=" + filePath + "/" + file.name} target={"_blank"}>
                            <button type={"button"}>Open</button>
                        </a>

                    </li>
                )
            }
        </ul>
    </div>
}

export default FilesPage

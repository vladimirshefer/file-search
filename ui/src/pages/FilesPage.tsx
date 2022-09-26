import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useLocation} from "react-router-dom";

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
    let urlPath = useLocation();
    let [filePath, setFilePath] = useState<string>("")

    useEffect(() => {
        setFilePath(urlPath.pathname.substring("/files/".length));
    }, [urlPath])

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
        {"Hello!"}
        <ul>
            <p>
                Total directories: {directories.length}
            </p>
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
            <p>
                Total files: {files.length}
            </p>
            {
                files.map((file) =>
                    <li key={file.name}>
                        {file.name} : {file.size}
                    </li>
                )
            }
        </ul>
    </div>
}

export default FilesPage

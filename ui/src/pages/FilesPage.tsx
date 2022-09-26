import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useLocation, useParams} from "react-router-dom";

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
                        <Link to={"/edit/" + filePath + "/" + file.name} relative={"route"}>
                            Open
                        </Link>

                    </li>
                )
            }
        </ul>
    </div>
}

export default FilesPage

import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useLocation} from "react-router-dom";


function FilesPage() {
    let [files, setFiles] = useState<string[]>([]);
    let urlPath = useLocation();
    let [filePath, setFilePath] = useState<string>("")

    useEffect(() => {
        setFilePath(urlPath.pathname.substring("/files/".length));
    }, [urlPath])

    useEffect(() => {
        loadFiles(filePath)
    }, [filePath])

    async function loadFiles(filePath: string) {
        let response = await axios.get("/api/files/list", {
            params: {
                path: filePath
            }
        });
        let filesBody: string[] = response.data.files;
        setFiles(filesBody)
    }

    return <div>
        {"Hello!"}
        <ul>
            {filePath !== "" ? (
                <li key={".."}>
                    <Link to={".."} relative={"path"}>..</Link>
                </li>
            ) : ("")}
            {
                files.map((filename) =>
                    <li key={filename}>
                        <Link to={"./" + filename} relative={"path"}> {filename}</Link>
                    </li>
                )
            }
        </ul>
    </div>
}

export default FilesPage

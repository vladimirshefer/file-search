import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useParams} from "react-router-dom";
import "styles/FileEditPage.css"

function FileEditPage() {
    let {"*": filePath = ""} = useParams<string>()
    let [content, setContent] = useState<string>("")

    useEffect(() => {
        loadContent(filePath)
    }, [filePath])

    async function loadContent(filePath: string) {
        let response = await axios.get("/api/files/content", {
            params: {
                path: filePath
            }
        });
        setContent(response.data.content)
    }

    return (
        <div>
            <h1>
                {"File editor!"}
            </h1>
            <p>
                <Link to={"/files/" + filePath.substring(0, filePath.lastIndexOf("/"))}> back </Link>
            </p>
            <textarea value={content} onChange={(e) => setContent(e.target.value)} rows={50}></textarea>
        </div>
    )
}

export default FileEditPage

import React, {useEffect, useState} from 'react'
import axios from "axios";
import {Link, useLocation, useParams} from "react-router-dom";

function FileEditPage() {
    let urlPath = useLocation();
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
            <pre>
                {content}
            </pre>
        </div>
    )
}

export default FileEditPage

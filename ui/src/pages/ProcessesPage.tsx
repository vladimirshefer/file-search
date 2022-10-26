import axios from "axios";
import {useEffect, useState} from "react";
import {useSearchParams} from "react-router-dom";

export default function ProcessesPage() {

    let [data, setData] = useState<any>(null)
    let [queryParams] = useSearchParams()

    useEffect(() => {
        let id = setInterval(async () => {
            let ids = queryParams.getAll("ids")
            console.log(ids)
            setData((await axios.get("/api/processes",
                {
                    params: {ids: ids},
                    paramsSerializer: {indexes: null}
                }
            )).data)
        }, 500);

        return () => clearInterval(id);
    }, [])

    function renderList(node: any) {
        if (!node) {
            return <p>Empty</p>
        }
        if (Array.isArray(node)) {
            return <ul className={"border-2"}>
                <p>Array</p>
                {node.map((child: any) => renderList(child))}
            </ul>
        }
        if (!!node.status) {
            // if (node.status == "SUCCESS") return <p>SUCCESS</p>
            return <div className={"border-2"}>
                <h4>{node.id}</h4>
                <h4>{node.status || "NO STATUS"}</h4>
                {(!!node.children && node.children.length > 0)
                    ? (renderList(node.children))
                    : (
                        <>
                            <pre className={"whitespace-pre-wrap"}>{node.output || "NO OUTPUT"}</pre>
                            <pre className={"whitespace-pre-wrap"}>{node.errorOutput || "NO OUTPUT"}</pre>
                        </>
                    )
                }
            </div>
        } else return <pre className={"border-2"}>{JSON.stringify(node, null, 2)}</pre>

    }

    return renderList(data)

}

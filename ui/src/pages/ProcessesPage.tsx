import axios from "axios";
import {useEffect, useState} from "react";

export default function ProcessesPage() {

    let [data, setData] = useState<any>(null)

    useEffect(() => {
        let id = setInterval(async () => {
            setData((await axios.get("/api/processes")).data)
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
        if (!!node.first || !!node.second) {
            return <li className={"border-2"}>
                <p>{node.first}</p>
                <ul className={"border-2"}>{renderList(node.second)}</ul>
            </li>
        }
        if (!!node.chain) {
            return <ul className={"border-2"}>
                <p>Chain ({node.chain.length})</p>
                {
                    node.chain.map((child: any) => renderList(child))
                }
            </ul>
        }
        if (!!node.status) {
            if (node.status == "SUCCESS") return <p>SUCCESS</p>
            return <div className={"border-2"}>
                <h4>{node.status || "NO STATUS"}</h4>
                <pre className={"whitespace-pre-wrap"}>
                    {node.output || "NO OUTPUT"}
                </pre>
                <pre className={"whitespace-pre-wrap"}>
                    {node.errorOutput || "NO OUTPUT"}
                </pre>
            </div>
        }
        else return <pre className={"border-2"}>{JSON.stringify(node, null, 2)}</pre>

    }

    return renderList(data)

}

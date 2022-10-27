import axios from "axios";
import {useSearchParams} from "react-router-dom";
import "styles/ProcessesPage.css"
import {useQuery} from "@tanstack/react-query";

export default function ProcessesPage() {

    let [queryParams] = useSearchParams()

    let {
        data: processes,
        isLoading: dataLoading,
        isError: dataLoadingError,
    } = useProcesses(queryParams.getAll("ids"))

    if (dataLoading) {
        return <span>"Processes are loading..."</span>
    }

    if (dataLoadingError) {
        return <span>"Processes are loading..."</span>
    }

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

    function BackgroundWrapper(
        {
            backgroundClass,
            children,
        }: {
            backgroundClass: string,
            children: any
        }
    ) {
        return <div className="background-wrapper">
            <div className={`background-wrapper_background ${backgroundClass}`}></div>
            {children}
        </div>
    }

    return <>
        <BackgroundWrapper backgroundClass={"process-loading-background"}>
            <p>Loading</p>
        </BackgroundWrapper>

        {renderList(processes)}
    </>

}

function useProcesses(ids: string[]) {
    return useQuery(["processes"], async () => {
        let axiosResponse = await axios.get("/api/processes",
            {
                params: {ids: ids},
                paramsSerializer: {indexes: null}
            }
        );
        return axiosResponse.data
    }, {
        refetchInterval: 3000
    });
}

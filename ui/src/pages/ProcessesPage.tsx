import axios from "axios";
import {useSearchParams} from "react-router-dom";
import "styles/ProcessesPage.css"
import {useQuery} from "@tanstack/react-query";
import {useMemo} from "react";
import {Dialog} from '@headlessui/react'

interface ProcessInfo {
    id: string
    children: ProcessInfo[]
    status: string
    output: string
    errorOutput: string
}


export default function ProcessesPage() {

    let [queryParams, setQueryParams] = useSearchParams()

    let {
        data: processes,
    } = useProcesses("processes", queryParams.getAll("ids"))

    let open = useMemo(() => queryParams.get("open") || null, [queryParams]);

    let {
        data: openedProcess,
    } = useQuery(["openedProcesses", open], async () => {
        if (!open) return [];

        let axiosResponse = await axios.get("/api/processes", {
            params: {ids: open},
        });
        return axiosResponse.data as ProcessInfo[]
    }, {
        refetchInterval: 3000,
        select: (response: ProcessInfo[]) => {
            return (!!response) ? response[0] : undefined
        }
    })

    function ProcessItem(
        {
            processInfo
        }: {
            processInfo: ProcessInfo
        }
    ) {
        function bg() {
            switch (processInfo.status) {
                case "SUCCESS":
                    return "bg-lime-100"
                case "CANCELED":
                    return "bg-stone-100"
                case "ERROR":
                    return "bg-red-100"
                case "PENDING":
                    return "bg-sky-100"
                default:
                    return ""
            }
        }

        return <BackgroundWrapper backgroundClass={"process-loading-background"}>
            <div className={`border-2 ${(bg())}`}>
                <h4>
                    <button
                        tabIndex={0}
                        onClick={() => openProcess(processInfo.id)}
                        onKeyDown={(e) => {
                            if (e.key === "Enter" || e.key === "Space") openProcess(processInfo.id)
                        }}
                    >
                        {processInfo.id}
                    </button>
                </h4>

                {
                    (!!processInfo.children && processInfo.children.length > 0) ? (
                        <details>
                            <summary tabIndex={0}>Children</summary>
                            <ProcessList processInfos={processInfo.children || []}/>
                        </details>
                    ) : null
                }
            </div>
        </BackgroundWrapper>
    }

    function ProcessList(
        {
            processInfos
        }: {
            processInfos: ProcessInfo[]
        }
    ) {
        return <ul className={"ml-2"}>
            {
                processInfos.map(processInfo =>
                    <li key={processInfo.id}>
                        <ProcessItem processInfo={processInfo}/>
                    </li>
                )
            }
        </ul>
    }

    function closeProcess() {
        let newParams = {...queryParams} as any;
        delete newParams.open
        setQueryParams(newParams)
    }

    function openProcess(id: string) {
        setQueryParams({...queryParams, open: id})
    }

    return <>
        <div className={"toolbox flex"}>
            <div className={"dropdown"}>
                <button>
                    Button...
                </button>
            </div>
        </div>

        <div>
            {
                !openedProcess ? null : (
                    <Dialog
                        open={!!openedProcess}
                        onClose={closeProcess}
                        className={"relative z-50"}
                    >
                        <div className="fixed inset-0 bg-black/30" aria-hidden="true"/>
                        <div className="fixed inset-0 flex items-center justify-center p-4">
                            <Dialog.Panel className="w-full max-w-md rounded-lg bg-white p-5">
                                <Dialog.Title>
                                    Process {openedProcess.id} {openedProcess.status}
                                </Dialog.Title>
                                <Dialog.Description>
                                    This process has ({openedProcess.children?.length || 0}) children
                                </Dialog.Description>

                                <details>
                                    <summary>Log</summary>
                                    <p>Output log</p>
                                    <textarea
                                        className={"terminal"}
                                        value={openedProcess.output}
                                        rows={10}
                                    >
                                    </textarea>
                                    <p>Error log</p>
                                    <textarea
                                        className={"terminal"}
                                        value={openedProcess.errorOutput}
                                        rows={10}
                                    >
                                    </textarea>
                                </details>

                                <button onClick={() => closeProcess()}>Cancel</button>
                                <button onClick={() => closeProcess()}>Close</button>
                            </Dialog.Panel>
                        </div>
                    </Dialog>
                )
            }
        </div>
        <ProcessList processInfos={processes || []}/>
    </>

}

function useProcesses(key: string, ids: string[]) {
    return useQuery([key, ids], async () => {
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

import React, {useEffect, useState} from 'react'
import {useNavigate, useParams, useSearchParams} from "react-router-dom";
import {BsGrid3X3} from "react-icons/bs";
import {RiDeleteBin6Line} from "react-icons/ri";
import {GrOptimize} from "react-icons/gr";
import {MdList} from "react-icons/md";

import "styles/FilesPage.css"
import "components/toolbox/Toolbox.css"
import ConversionUtils from "utils/ConversionUtils";
import {MediaInfo} from "lib/Api";
import MediaCardGrid from "components/FilesPage/media/MediaCardGrid";
import Breadcrumbs from "components/files/BreadCrumbs";
import DirectoryCard from "components/FilesPage/directories/DirectoryCard";
import {Readme} from "components/files/Readme";
import FilesList from "components/FilesPage/FilesList/FilesList";
import FileApiService from "lib/service/FileApiService";
import DragArea from "components/drag/DragArea";
import Sidebar from "components/modal/Sidebar";
import ImageView from "components/FilesPage/ImageView/ImageView";
import {ViewType} from 'enums/view';
import {useQuery} from "@tanstack/react-query";
import mime from "mime";

function Inspection(
    {
        inspection
    }: {
        inspection: any
    }
) {
    let fileApiService = new FileApiService()

    function fixInspection(inspection: any) {
        return fileApiService.fixInspection(inspection)
    }

    return <tr>
        <td>
            <button
                onClick={() => fixInspection(inspection).then(it => alert(JSON.stringify(it)))}
            >
                Fix
            </button>
        </td>
        <td>{inspection.type}</td>
        <td>{inspection.description}</td>
        <td>{inspection.path}</td>
    </tr>;
}

function Inspections(
    {
        filePath
    }: {
        filePath: string
    }
) {

    let fileApiService = new FileApiService()

    let [inspectionsRequested, setInspectionsRequested] = useState(false)

    let {
        data: inspections,
        isLoading: inspectionsLoading,
        error: inspectionsLoadingError,
    } = useQuery(["inspections", inspectionsRequested, filePath], async () => {
        return inspectionsRequested ? await fileApiService.loadInspections(filePath) as any[] : []
    })

    return <>
        Inspections...
        <button onClick={() =>
            setInspectionsRequested(true)
        }>
            Load
        </button>
        <div>
            {!!inspections
                ? <>
                    <table>
                        <tbody>
                        {inspections.map(inspection => {
                            return <Inspection key={inspection.path} inspection={inspection}/>
                        })}
                        </tbody>
                    </table>
                </>
                : <span> All fine </span>}
        </div>
    </>
}

function FilesPage() {
    let [stats, setStats] = useState<{ [key: string]: any }>({});
    let {"*": filePath = ""} = useParams<string>()
    let [pathSegments, setPathSegments] = useState<string[]>([])
    let navigate = useNavigate();
    let [selectedFiles, setSelectedFiles] = useState<string[]>([])
    let fileApiService = new FileApiService()
    let [searchParams, setSearchParams] = useSearchParams();
    let [stateView, switchView] = useState<ViewType>(ViewType.Grid);

    let {
        data: content,
        isLoading: contentLoading,
        error: contentLoadingError,
    } = useQuery(["content", filePath], async () => {
        return await fileApiService.loadContent(filePath)
    })

    let {
        data: readme2,
        isLoading: readmeIsLoading,
        isLoadingError: readmeIsError,
    } = useQuery(["readme", filePath], async () => {
        return await fileApiService.loadReadme(filePath)
    })

    async function init() {
        try {
            await loadStats(filePath)
        } catch (e) {
            console.log(e);
        }
    }

    useEffect(() => {
        setPathSegments(filePath.split("/").filter(it => !!it))
        setSelectedFiles([])
        init();
        return function cleanup() {
            setSelectedFiles([]);
            setStats({})
        }
    }, [filePath])

    let openedMedia: string | null = null;
    let openedMediaCandidate = searchParams.get("open");
    if (!!openedMediaCandidate) openedMedia = openedMediaCandidate

    async function loadStats(filePath: string) {
        setStats(await fileApiService.loadStats(filePath) || {})
    }

    function renderStats(stats: { [p: string]: any }) {
        return <>
            <p>Forbidden directories: {stats["forbiddenDirectories"]}</p>
            <p>Total size: {ConversionUtils.getReadableSize(stats["totalSize"])}</p>
        </>
    }

    let imageFiles: MediaInfo[] = content?.files?.filter(it => {
        let type: string = (it.source || it.optimized)!!.type;
        return ["jpg", "png", "jpeg"].includes(type)
    }) || []

    function goToPathSegment(n: number) {
        navigate("/files/" + pathSegments.slice(0, n).join("/"))
    }

    function openSubdirectory(name: string) {
        navigate("/files/" + pathSegments.join("/") + (!pathSegments ? "/" : "") + name)
    }

    function openMedia(fileName: string) {
        setSearchParams({...searchParams, open: fileName})
    }

    function closeMedia() {
        let newParams = {...searchParams} as any;
        delete newParams.open
        setSearchParams(newParams)
    }

    async function initOptimizationForSelected() {
        try {
            await fileApiService.optimize(filePath, selectedFiles)
        } catch (e) {
            alert("Could not init optimization")
        }
    }

    function deleteSelected() {
        alert(`TODO delete ${selectedFiles}`)
        return null;
        // return fileApiService.delete();
    }

    let openedMediaType = !!openedMedia ? mime.getType(openedMedia) : null;

    return <div>
        {(!!openedMedia) ? (
            <Sidebar
                isVisible={!!openedMedia}
                actionClose={() => closeMedia()}
            >
                {
                    openedMediaType?.includes("video/")
                        ? <video
                            src={`/api/files/show/?rootName=source,optimized&path=${filePath}/${openedMedia}`}
                            controls={true}
                            autoPlay={true}
                            loop={true}
                        />
                        : openedMediaType?.includes("image/")
                            ? <ImageView
                                // TODO use only files from corresponding root. here is the temp fix to ui not fail
                                image1Url={`/api/files/show/?rootName=source,optimized&path=${filePath}/${openedMedia}`}
                                image2Url={`/api/files/show/?rootName=optimized,source&path=${filePath}/${openedMedia}`}
                            />
                            : null
                }
            </Sidebar>
        ) : null
        }
        <div className="toolbox flex">
            <Breadcrumbs
                names={["Home", ...pathSegments]}
                selectFn={i => goToPathSegment(i)}
            />
            <div className={"file-actions-bar"} key={"file-actions-bar"}>
                <div className={"toolbar-item"}>
                    {stateView === ViewType.Grid ?
                        <button onClick={() => switchView(ViewType.List)}>
                            <BsGrid3X3 className={"toolbar-icon"} title={"Grid"}/>
                        </button>
                        : <button onClick={() => switchView(ViewType.Grid)}>
                            <MdList className={"toolbar-icon"} title={"List"}/>
                        </button>}
                </div>
                <div className={"toolbar-item"}>
                    <button onClick={initOptimizationForSelected}>
                        <GrOptimize
                            className={"toolbar-icon"}
                            title={"Optimize"}/>
                    </button>
                </div>
                <div className={"toolbar-item"}>
                    <button onClick={deleteSelected}>
                        <RiDeleteBin6Line
                            className={"toolbar-icon"}
                            title={"Delete"}/>
                    </button>
                </div>
            </div>
        </div>
        <Readme readme={readme2}/>
        {contentLoading
            ? (<span>LOADING...</span>)
            : !!contentLoadingError
                ? (<span>LOADING ERROR</span>)
                : (
                    <>
                        <Inspections filePath={filePath}/>
                        <DragArea
                            setSelectedItems={setSelectedFiles}
                        >
                            <DirectoryCard
                                directories={content?.directories || []}
                                path={filePath}
                                selectedDirectories={selectedFiles}
                                actionOpen={(directoryName) => openSubdirectory(directoryName)}
                                isView={stateView}
                            />
                            <MediaCardGrid
                                imageMedias={imageFiles || []}
                                path={filePath}
                                selectedItems={selectedFiles}
                                actionOpen={(fileName) => openMedia(fileName)}
                            />
                            <FilesList
                                files={content?.files || []}
                                root={filePath}
                                filesSelected={selectedFiles}
                                actionOpen={(filename) => openMedia(filename)}
                            />
                        </DragArea>
                    </>
                )}
    </div>
}


export default FilesPage

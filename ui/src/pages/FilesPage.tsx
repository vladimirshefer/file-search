import React, { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { BsGrid3X2Gap } from "react-icons/bs";
import { RiDeleteBin6Line } from "react-icons/ri";
import { GrOptimize } from "react-icons/gr";
import { MdOutlineList } from "react-icons/md";
import { AiOutlineSecurityScan } from "react-icons/ai";

import "styles/FilesPage.css"
import "components/toolbox/Toolbox.css"
import ConversionUtils from "utils/ConversionUtils";
import { MediaInfo } from "lib/Api";
import MediaCardGrid from "components/FilesPage/media/MediaCardGrid";
import Breadcrumbs from "components/files/BreadCrumbs";
import ToggleableDirectoriesView from "components/FilesPage/directories/ToggleableDirectoriesView";
import { Readme } from "components/files/Readme";
import FilesList from "components/FilesPage/FilesList/FilesList";
import fileApiService from "lib/service/FileApiService";
import DragArea from "components/drag/DragArea";
import ModalWindow from "components/modal/ModalWindow";
import ImageView from "components/FilesPage/ImageView/ImageView";
import { ViewType } from 'enums/view';
import { useQuery } from "@tanstack/react-query";
import mime from "mime";
import Inspections from "./Inspections";

function MediaView(
    {
        fileName,
        filePath
    }: {
        fileName: string,
        filePath: string
    }
) {
    let openedMediaType = !!fileName ? mime.getType(fileName) : null

    if (openedMediaType?.includes("video/")) {
        return <div className={"w-full h-full"}>
            <video className={"max-h-[75vh] min-h-[50vh]"}
                src={`/api/files/show/?rootName=source,optimized&path=${filePath}/${fileName}#t=0.1`}
                controls={true}
                autoPlay={true}
                loop={true}
            />
        </div>;
    }

    function getImageCompareView() {
        return <ImageView
            // TODO use only files from corresponding root. here is the temp fix to ui not fail
            image1Url={`/api/files/show/?rootName=source,optimized&path=${filePath}/${fileName}`}
            image2Url={`/api/files/show/?rootName=optimized,source&path=${filePath}/${fileName}`}
        />;
    }

    return openedMediaType?.includes("image/")
        ? getImageCompareView()
        : null;

}

function FilesPage() {
    let [stats, setStats] = useState<{ [key: string]: any }>({});
    let { "*": filePath = "" } = useParams<string>()
    let [pathSegments, setPathSegments] = useState<string[]>([])
    let navigate = useNavigate();
    let [selectedFiles, setSelectedFiles] = useState<string[]>([])
    let [searchParams, setSearchParams] = useSearchParams()
    let [stateView, switchView] = useState<ViewType>(ViewType.Grid)
    let [inspectionsOpen, setInspectionsOpen] = useState<boolean>(false)

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
        let name: string = (it.source || it.optimized)!!.name;
        return mime.getType(name)?.includes("image/") || name.includes(".mp4")
    }) || []

    function goToPathSegment(n: number) {
        navigate("/files/" + pathSegments.slice(0, n).join("/"))
    }

    function openSubdirectory(name: string) {
        navigate("/files/" + pathSegments.join("/") + (pathSegments.length > 0 ? "/" : "") + name)
    }

    function openMedia(fileName: string) {
        setSearchParams({ ...searchParams, open: fileName })
    }

    function closeMedia() {
        let newParams = { ...searchParams } as any;
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


    return <div>
        {(!!openedMedia) ? (
            <ModalWindow
                isVisible={!!openedMedia}
                actionClose={() => closeMedia()}
            >
                <div className={"grid grid-cols-12 w-full h-full"}>
                    <div className={"col-span-2"}>
                        <button className={"w-full h-full"} onClick={() => {
                            let index = imageFiles.map(it => it.source?.name).indexOf(openedMedia!!)
                            let nextFileName = imageFiles[index - 1].source?.name
                            if (nextFileName) {
                                openMedia(nextFileName)
                            } else {
                                alert("This is last media")
                            }
                        }}>
                            Prev
                        </button>
                    </div>
                    <div className={"col-span-8 place-content-center"}>
                        <MediaView fileName={openedMedia} filePath={filePath}/>
                    </div>
                    <div className={"col-span-2"}>
                        <button className={"w-full h-full"} onClick={() => {
                            let index = imageFiles.map(it => it.source?.name).indexOf(openedMedia!!)
                            let nextFileName = imageFiles[index + 1].source?.name
                            if (nextFileName) {
                                openMedia(nextFileName)
                            } else {
                                alert("This is last media")
                            }
                        }}>
                            Next
                        </button>
                    </div>
                </div>
            </ModalWindow>
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
                            <BsGrid3X2Gap className={"toolbar-icon"} title={"Grid"}/>
                        </button>
                        : <button onClick={() => switchView(ViewType.Grid)}>
                            <MdOutlineList className={"toolbar-icon"} title={"List"}/>
                        </button>}
                </div>
                <div className={"toolbar-item"}>
                    <button onClick={() => {
                        setInspectionsOpen(true)
                    }}>
                        <AiOutlineSecurityScan
                            className={"toolbar-icon"}
                            title={"Inspect files"}/>
                    </button>
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
                        <ModalWindow isVisible={inspectionsOpen} actionClose={() => setInspectionsOpen(false)}>
                            <Inspections filePath={filePath}/>
                        </ModalWindow>
                        <DragArea
                            setSelectedItems={setSelectedFiles}
                        >
                            <ToggleableDirectoriesView
                                directories={content?.directories || []}
                                path={filePath}
                                selectedDirectories={selectedFiles}
                                actionOpen={(directoryName: string) => openSubdirectory(directoryName)}
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

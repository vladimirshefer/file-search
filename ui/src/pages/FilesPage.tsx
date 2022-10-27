import React, {useEffect, useState} from 'react'
import {useNavigate, useParams, useSearchParams} from "react-router-dom";
import {AiOutlineDelete} from "react-icons/ai";
import {BsPlay} from "react-icons/bs";
import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { RiDeleteBin6Line } from "react-icons/ri";
import { GrOptimize } from "react-icons/gr";
import { BsGrid3X3 } from "react-icons/bs";
import { MdList } from "react-icons/md";

import "styles/FilesPage.css"
import "components/toolbox/Toolbox.css"
import ConversionUtils from "utils/ConversionUtils";
import {MediaDirectoryInfo, MediaInfo} from "lib/Api";
import MediaCardGrid from "components/FilesPage/media/MediaCardGrid";
import Breadcrumbs from "components/files/BreadCrumbs";
import DirectoryCardGrid from "components/FilesPage/directories/DirectoryCardGrid";
import {Readme} from "components/files/Readme";
import FilesList from "components/FilesPage/FilesList/FilesList";
import FileApiService from "lib/service/FileApiService";
import DragArea from "components/drag/DragArea";
import Sidebar from "components/modal/Sidebar";
import ImageView from "components/FilesPage/ImageView/ImageView";
import { ViewType } from 'enums/view';
import {useQuery} from "@tanstack/react-query";

function FilesPage() {
    let [stats, setStats] = useState<{ [key: string]: any }>({});
    let [readme, setReadme] = useState<string>("");
    let {"*": filePath = ""} = useParams<string>()
    let [pathSegments, setPathSegments] = useState<string[]>([])
    let navigate = useNavigate();
    let [selectedFiles, setSelectedFiles] = useState<string[]>([])
    let fileApiService = new FileApiService()
    let [searchParams, setSearchParams] = useSearchParams();
    let [stateView, switchedView] = useState<ViewType>(ViewType.Grid);

    let {
        isLoading: contentLoading,
        error: contentLoadingError,
        data: content,
    } = useQuery(["content"], async () => {
        return await fileApiService.loadContent(filePath) as (MediaDirectoryInfo | null)
    })

    async function init() {
        setPathSegments(filePath.split("/").filter(it => !!it))
        setSelectedFiles([])
        try {
            await loadStats(filePath)
            await loadReadme(filePath)
        } catch (e) {
            console.log(e);
        }

        return function cleanup() {
            setSelectedFiles([]);
            setStats({})
            setReadme("")
        }
    }

    useEffect(() => {
        init();
    }, [filePath])

    if (contentLoading) return <span>LOADING...</span>

    let openedMedia: string | null = null;
    let openedMediaCandidate = searchParams.get("open");
    if (!!openedMediaCandidate) openedMedia = openedMediaCandidate

    async function loadStats(filePath: string) {
        setStats(await fileApiService.loadStats(filePath) || {})
    }

    async function loadReadme(filePath: string) {
        setReadme(await fileApiService.loadReadme(filePath) || "")
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

    return <div>
        {(!!openedMedia) ? (
            <Sidebar
                isVisible={!!openedMedia}
                actionClose={() => closeMedia()}
            >
                <ImageView
                    // TODO use only files from corresponding root. here is the temp fix to ui not fail
                    image1Url={"/api/files/show/?rootName=source,optimized&path=" + filePath + "/" + openedMedia}
                    image2Url={"/api/files/show/?rootName=optimized,source&path=" + filePath + "/" + openedMedia}
                />

            </Sidebar>
        ) : null
        }
        <div className="toolbox flex">
            <Breadcrumbs
                names={["/", ...pathSegments]}
                selectFn={i => goToPathSegment(i)}
            />
            <div className={"file-actions-bar"}>
                <div className={"toolbar-item"}>
                    {stateView === ViewType.Grid ?
                        <BsGrid3X3 className={"toolbar-icon"}
                            onClick={() => switchedView(ViewType.List)} title={"Grid"} />
                        : <MdList className={"toolbar-icon"}
                            onClick={() => { switchedView(ViewType.Grid) }} title={"List"} />}
                </div>
                <div className={"toolbar-item"}>
                    <GrOptimize
                        className={"toolbar-icon"}
                        onClick={initOptimizationForSelected}
                        title={"Optimize"} />
                </div>
                <div className={"toolbar-item"}>
                    <RiDeleteBin6Line
                        className={"toolbar-icon"}
                        title={"Delete"}
                        onClick={deleteSelected} />
                </div>
            </div>
        </div>
        <Readme readme={readme}/>
        {contentLoading
            ? (<span>LOADING...</span>)
            : contentLoadingError
                ? (<span>LOADING ERROR</span>)
                : (
                    <DragArea
                        setSelectedItems={setSelectedFiles}
                    >
                        <DirectoryCardGrid
                            directories={content?.directories || []}
                            path={filePath}
                            selectedDirectories={selectedFiles}
                            actionOpen={(dirname) => null}
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
                )}
    </div>
}

export default FilesPage

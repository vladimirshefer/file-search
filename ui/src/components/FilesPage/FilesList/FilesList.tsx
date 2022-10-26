import {MediaInfo} from "lib/Api";
import ConversionUtils from "utils/ConversionUtils";
import {Link} from "react-router-dom";
import React from "react";
import "./FilesList.css"

export default function FilesList(
    {
        files,
        root,
        filesSelected = [],
        actionOpen = (_) => undefined
    }: {
        files: MediaInfo[],
        root: string,
        filesSelected?: string[],
        actionOpen: (filename: string) => void,
    }
) {

    return <div className={"file-tree_files-list"}>
        <ul className="files-list">
            <p className={"files-list_header"}>
                All files ({files.length})
            </p>
            {files.map((file) => {
                    let sourceSize = ConversionUtils.getReadableSize(file.source?.size || null);
                    let optimizedSize = ConversionUtils.getReadableSize(file.optimized?.size || null);
                    let size = `${sourceSize}/${optimizedSize}`;
                    let editLink = "/edit/" + root + "/" + file.displayName;
                    let openLink = "/api/files/show/?path=" + root + "/" + file.displayName;
                    let openOptimizedLink = "/api/files/show/?rootName=optimized&path=" + root + "/" + file.displayName;
                    return <FileInfo
                        key={file.displayName}
                        displayName={file.displayName}
                        secondaryName={file.optimized?.name || null}
                        size={size}
                        editLink={editLink}
                        openLink={openLink}
                        openOptimizedLink={openOptimizedLink}
                        isSelected={filesSelected.includes(file.displayName)}
                        actionOpen={() => actionOpen(file.displayName)}
                    />;
                }
            )}
        </ul>
    </div>;
}

/** Pure component **/
function FileInfo(
    {
        displayName,
        secondaryName,
        size,
        editLink,
        openLink,
        openOptimizedLink,
        isSelected,
        actionOpen = () => undefined,
    }: {
        displayName: string,
        secondaryName: string | null,
        size: string,
        editLink: string,
        openLink: string,
        openOptimizedLink: string,
        isSelected: boolean,
        actionOpen: () => void,
    }
) {
    return <li
        key={displayName}
        className={`file-info drag-selectable ${isSelected ? "file-info__selected" : ""}`}
        data-selection-id={displayName}
    >
            <span className={"file-info_name"}>
                {displayName}
            </span>
        {
            (!secondaryName) ?
                <span className={"file-info_name-optimized"}>
                        {secondaryName}
                    </span>
                : null
        }
        <span className={"file-info_size"}>
            {size}
        </span>
        <Link
            to={editLink}
            relative={"route"}
            className={"file-info_button"}
        >
            <button type={"button"}>Edit text</button>
        </Link>
        <a
            href={openLink}
            target={"_blank"}
            className={"file-info_button"}
        >
            <button type={"button"}>Open source</button>
        </a>
        <a
            href={openOptimizedLink}
            target={"_blank"}
            className={"file-info_button"}
        >
            <button type={"button"}>Optimized</button>
        </a>
        <button type={"button"}
                onClick={actionOpen}
        >
            Open
        </button>
    </li>;
}

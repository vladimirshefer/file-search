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
    }: {
        files: MediaInfo[],
        root: string,
        filesSelected?: string[],
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
                    return <FileInfo
                        displayName={file.displayName}
                        secondaryName={file.optimized?.name || null}
                        size={size}
                        editLink={editLink}
                        openLink={openLink}
                        isSelected={filesSelected.includes(file.displayName)}
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
        isSelected,
    }: {
        displayName: string,
        secondaryName: string | null,
        size: string,
        editLink: string,
        openLink: string,
        isSelected: boolean
    }
) {
    return <li
        key={displayName}
        className={`file-info drag-selectable ${isSelected?"file-info__selected":""}`}
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
            <button type={"button"}>Open</button>
        </a>
    </li>;
}

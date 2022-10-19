import {MediaInfo} from "lib/Api";
import ConversionUtils from "utils/ConversionUtils";
import {Link} from "react-router-dom";
import React from "react";
import "./FilesList.css"

export default function FilesList(
    {
        files,
        root,
    }: {
        files: MediaInfo[],
        root: string,
    }
) {

    return <div className={"file-tree_files-list"}>
        <p className={"files-list_header"}>
            Total files: {files.length}
        </p>
        <ul className="files-list">
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
    }: {
        displayName: string,
        secondaryName: string | null,
        size: string,
        editLink: string,
        openLink: string,
    }
) {
    return <li
        key={displayName}
        className={"file-info drag-selectable"}
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

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
            {files.map((file) =>
                <FileInfo file={file} root={root}/>
            )}
        </ul>
    </div>;
}

function FileInfo(
    {
        file,
        root,
    }: {
        file: MediaInfo,
        root: string,
    }
) {
    let filename = file.displayName;
    return <li
        key={filename}
        className={"file-info drag-selectable"}
        data-selection-id={file.displayName}
    >
            <span className={"file-info_name"}>
                {filename}
            </span>
        {
            (!!file.optimized) ?
                <span className={"file-info_name-optimized"}>
                        {file.optimized.name}
                    </span>
                : null
        }
        <span className={"file-info_size"}>
                {ConversionUtils.getReadableSize(file.source?.size || null)}
            /
            {ConversionUtils.getReadableSize(file.optimized?.size || null)}
            </span>
        <Link
            to={"/edit/" + root + "/" + filename}
            relative={"route"}
            className={"file-info_button"}
        >
            <button type={"button"}>Edit text</button>
        </Link>
        <a
            href={"/api/files/show/?path=" + root + "/" + filename}
            target={"_blank"}
            className={"file-info_button"}
        >
            <button type={"button"}>Open</button>
        </a>
    </li>;
}

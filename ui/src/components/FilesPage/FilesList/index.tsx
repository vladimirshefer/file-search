import {MediaInfo} from "lib/Api";
import ConversionUtils from "utils/ConversionUtils";
import {Link} from "react-router-dom";
import React from "react";
import "./index.css"

export function FilesList(
    {
        files,
        root
    }: {
        files: MediaInfo[],
        root: string
    }
) {
    function FileInfo(file: MediaInfo) {
        let filename = file.source?.name || file.optimized?.name;
        return <li key={filename} className={"file-info"}>
            <span className={"file-info_name"}>
                {filename}
            </span>
            <span className={"file-info_name"}>
                {filename}
            </span>
            {(!!file.optimized) ?
                <span className={"file-info_name-optimized"}>
                    file.optimized.name
                </span>
                : null}
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

    return <div className={"file-tree_files-list"}>
        <p className={"files-list_header"}>
            Total files: {files.length}
        </p>
        <ul className="files-list">
            {files.map((file) => FileInfo(file))}
        </ul>
    </div>;
}

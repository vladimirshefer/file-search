import {MediaDirectoryInfo} from "lib/Api";
import DirectoryCard from "components/FilesPage/DirectoryCard";
import React from "react";
import "./DirectoryCardGrid.css"

export default function DirectoryCardGrid(
    {
        directories,
        path,
        actionOpen = (_) => null,
    }: {
        directories: MediaDirectoryInfo[],
        path: string,
        actionOpen: (directoryName: string) => void
    }
) {

    return <>
        <ul className="directory-card-grid">
            <p className={"directory-card-grid_header"}>
                Directories ({directories.length})
            </p>
            {directories.map((directory) =>
                <DirectoryCard
                    name={directory.name}
                    parent={path}
                    key={directory.name}
                    actionOpen={() => actionOpen(directory.name)}
                />
            )}
        </ul>
    </>
}

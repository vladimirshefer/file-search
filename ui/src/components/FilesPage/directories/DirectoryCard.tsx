import { MediaDirectoryInfo } from "lib/Api";
import DirectoryCardGrid from "components/FilesPage/directories/DirectoryCardGrid";
import DirectoryCardList from "components/FilesPage/directories/DirectoryCardList";
import { ViewType } from 'enums/view';
import "./DirectoryCard.css"

export default function DirectoryCard(
    {
        directories,
        path,
        selectedDirectories = [],
        actionOpen = (_) => null,
        isView,
    }: {
        directories: MediaDirectoryInfo[],
        path: string,
        selectedDirectories?: string[]
        actionOpen: (directoryName: string) => void,
        isView: ViewType
    }
) {

    return <>
        <ul className={isView === ViewType.Grid ? "directory-card-grid" : "directory-card-list"}>
            <p className={isView === ViewType.Grid ?  "directory-card-grid_header" : "directory-card-list_header"}>
                Directories ({directories.length})
            </p>
            {isView === ViewType.Grid ?
                directories.map((directory) =>
                    <DirectoryCardGrid
                        name={directory.name}
                        parent={path}
                        key={directory.name}
                        isSelected={selectedDirectories.includes(directory.name)}
                        actionOpen={() => actionOpen(directory.name)}
                    />
                ) : directories.map((directory) =>
                    <DirectoryCardList
                        name={directory.name}
                        parent={path}
                        key={directory.name}
                        isSelected={selectedDirectories.includes(directory.name)}
                        actionOpen={() => actionOpen(directory.name)}
                    />
                )}
        </ul>
    </>
}

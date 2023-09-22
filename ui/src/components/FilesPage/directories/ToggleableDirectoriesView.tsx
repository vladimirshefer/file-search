import { MediaDirectoryInfo } from "lib/Api";
import GridDirectoryView from "components/FilesPage/directories/GridDirectoryView";
import ListDirectoryView from "components/FilesPage/directories/ListDirectoryView";
import { ViewType } from 'enums/view';
import "./DirectoryCard.css"

export default function ToggleableDirectoriesView(
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
        <ul className={`grid grid-cols-12 p-3 gap-2`}>
            <p className={`col-span-12 p-3 text-lg font-bold`}>
                Directories ({directories.length})
            </p>
            {isView === ViewType.Grid ?
                directories.map((directory) =>
                    <GridDirectoryView
                        name={directory.name}
                        parent={path}
                        key={directory.name}
                        isSelected={selectedDirectories.includes(directory.name)}
                        actionOpen={() => actionOpen(directory.name)}
                    />
                ) : directories.map((directory) =>
                    <ListDirectoryView
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

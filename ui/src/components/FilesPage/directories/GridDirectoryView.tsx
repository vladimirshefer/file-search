import { useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import ConversionUtils from "utils/ConversionUtils";
import "./DirectoryCardGrid.css"
import { GoFileDirectory } from "react-icons/go";
import { AiOutlineInfoCircle } from "react-icons/ai";

export default function GridDirectoryView(
    {
        name,
        parent,
        isSelected = false,
        actionOpen = () => undefined,
    }: {
        name: string,
        parent: string,
        isSelected?: boolean,
        actionOpen?: () => void
    }) {

    let [size, setSize] = useState<number | null>(null)

    async function requestSize() {
        let result = await axios.get("/api/files/size", {
            params: {
                path: parent + "/" + name
            }
        });

        setSize(result.data?.size)
    }

    return <li
        key={name}
        className={`directory-card col-span-6 sm:col-span-4 md:col-span-3 lg:col-span-2 drag-selectable 
                    overflow-hidden rounded-md 
                    ${isSelected ? "bg-primary-200" : ""}`}
        data-selection-id={name}
        onDoubleClick={() =>
            actionOpen()
        }
        title={name}
    >

        <div className="directory-card_main-line">
            <div className={"directory-card_icon text-lg"}>
                <GoFileDirectory className={"text-lg"}/>
            </div>
            <span className={"truncate mx-2 cursor-default"}>
                {name}
            </span>
            <button className={"text-lg"}
                onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log("Directory info requested");
                }}
            ><AiOutlineInfoCircle/></button>
        </div>
        <div className={"directory-card_status-line"}>
                <span
                    className={"directory-card_status-item directory-card_size"}
                    onClick={(e) => {
                        e.preventDefault()
                        e.stopPropagation()
                    }}
                    onDoubleClick={(e) => {
                        e.preventDefault()
                        e.stopPropagation()
                        requestSize()
                    }}
                >
                    {ConversionUtils.getReadableSize(size)}
                </span>

            <Link
                to={`./${name}`}
                relative={"path"}
                title={name}
                onTouchEnd={() => {
                    actionOpen()
                }}
            >
                <span
                    className={"directory-card_status-item"}
                >
                    {"Open"}
                </span>
            </Link>

        </div>
    </li>;
}

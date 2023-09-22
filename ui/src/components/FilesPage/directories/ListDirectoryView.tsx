import { useState } from "react";
import axios from "axios";
import ConversionUtils from "utils/ConversionUtils";
import "./DirectoryCardList.css";
import { Link, useNavigate } from "react-router-dom";
import { GoFileDirectory } from "react-icons/go";

export default function ListDirectoryView(
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
    let [size, setSize] = useState<number | null>(null);

    const navigate = useNavigate();

    async function requestSize(name: string) {
        let result = await axios.get("/api/files/size", {
            params: {
                path: parent + "/" + name
            }
        });

        setSize(result.data?.size)
    }

    return <>
        <li key={name}
            className={`directory-list-info col-span-12 grid grid-cols-2 drag-selectable shadow
            ${isSelected ? "bg-primary-200" : ""}`}
            data-selection-id={name}>
            <Link
                draggable={false}
                to={"./" + name}
                relative={"path"}
                title={name}
                onTouchEnd={() => {
                    navigate("./" + name)
                }}
            >
                <div className="flex content-center items-center">
                    <div className={"mx-3 my-1"}>
                        <div className={"directory-card_icon text-lg"}>
                            <GoFileDirectory className={"text-lg"}/>
                        </div>
                    </div>
                    <span className={"directory-list-info-card_name"}>
                        {name}
                    </span>
                </div>
            </Link>
            <div className={`bg-primary-300 p-1
                flex justify-center 
                items-center content-center justify-self-end 
                col-span-2 md:col-span-3 lg:col-span-1
                cursor-pointer
                rounded-md
                text-sm
            `}>
                <span
                    className={"directory-list-card_status-item directory-card_size"}
                    onClick={(e) => {
                        e.preventDefault()
                        e.stopPropagation()
                    }}
                    onDoubleClick={(e) => {
                        e.preventDefault();
                        e.stopPropagation()
                        requestSize(name)
                    }}
                >
                    {ConversionUtils.getReadableSize(size)}
                </span>
            </div>
        </li>
    </>
}

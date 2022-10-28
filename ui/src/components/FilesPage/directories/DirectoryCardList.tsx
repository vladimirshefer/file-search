import { useState } from "react";
import axios from "axios";
import ConversionUtils from "utils/ConversionUtils";
import "./DirectoryCardList.css";
import { Link, useNavigate } from "react-router-dom";

export default function DirectoryCardGrid(
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
            className={`directory-list-info grid grid-cols-2 drag-selectable ds-selectable ${isSelected ? "directory-list-info__selected" : ""}`}
            data-selection-id={name}>
            <Link
                to={"./" + name}
                relative={"path"}
                title={name}
                onTouchEnd={() => {
                    navigate("./" + name)
                }}
            >
                <div className="directory-list-info-card_main-line">
                    <div className={"directory-list-card_icon m-3"}>
                        <svg
                            xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5}
                            stroke="currentColor" className="w-6 h-6">
                            <path strokeLinecap="round" strokeLinejoin="round"
                                d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
                        </svg>
                    </div>
                    <span className={"directory-list-info-card_name"}>
                        {name}
                    </span>
                </div>
            </Link>
            <div className={"directory-list-info-card_status-line"}>
                <span
                    className={"directory-list-card_status-item directory-card_size"}
                    onClick={(e) => {
                        e.preventDefault()
                    }}
                    onDoubleClick={(e) => {
                        e.preventDefault();
                        requestSize(name)
                    }}
                >
                    {ConversionUtils.getReadableSize(size)}
                </span>
            </div>
        </li>
    </>
}

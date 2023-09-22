import fileApiService from "../lib/service/FileApiService";
import React, { Fragment, useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";


function Inspection(
    {
        inspection
    }: {
        inspection: any
    }
) {


    let path = inspection.path as string;
    let idx = path.lastIndexOf("/")
    let directory = path.slice(0, idx)
    let filename = path.slice(idx + 1)


    function fixInspection(inspection: any) {
        return fileApiService.fixInspection(inspection)
    }

    return <Fragment key={`${inspection.path}_${inspection.type}`}
    >
        <li className={"grid grid-cols-12 border-b border-solid border-black-300 p-2"}>

            <div className={"col-span-12"}>
                <span className={"text-lg"}>{inspection.description}</span>
                <span className={"text-sm text-black-700 px-1"}>{inspection.type}</span>
            </div>
            <div className={"col-span-12 max-w-md"}>
                <Link to={`/files/${directory}?open=${filename}`}>
                    <span title={inspection.path}>{inspection.path}</span>
                </Link>
            </div>
            <div className={"col-span-12"}>
                <button className={"bg-blue-300 rounded-md px-3 py-1"}
                    onClick={() => fixInspection(inspection)/*.then(it => alert(JSON.stringify(it)))*/}
                >
                    Fix
                </button>
            </div>
        </li>
    </Fragment>;
}

function Inspections(
    {
        filePath
    }: {
        filePath: string
    }
) {

    let [inspectionsRequested, setInspectionsRequested] = useState(false)

    let {
        data: inspections,
        isLoading: inspectionsLoading,
        error: inspectionsLoadingError,
    } = useQuery({
        queryKey: ["inspections"],
        queryFn: async () => {
            return (await fileApiService.loadInspections(filePath)).slice(0, 20) as any[]
        },
        enabled: inspectionsRequested,
    })

    let inspectionsList = useMemo<any[]>(() => inspectionsRequested ? inspections || [] : [], [inspections, inspectionsRequested])

    return <div className={"bg-white p-3 w-full h-full overflow-scroll"}>
        <h3 className={"text-lg"}>
            Inspections
        </h3>
        <button
            className={"px-2 py-1 rounded-md bg-red-100 mr-1"}
            onClick={() =>
                setInspectionsRequested(true)
            }>
            Load
        </button>
        <button
            className={"px-2 py-1 rounded-md bg-red-100 mr-1"}
            onClick={() =>
                setInspectionsRequested(false)
            }>
            Close
        </button>
        <div>
            {inspectionsList.length > 0
                ? <ul className={""}>
                    {inspectionsList.map(inspection => {
                        return <Inspection key={inspection.path} inspection={inspection}/>
                    })}
                </ul>
                : <span> All fine </span>}
        </div>
    </div>
}

export default React.memo(Inspections)

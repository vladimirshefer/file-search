import fileApiService from "../lib/service/FileApiService";
import React, { useState } from "react";
import { useQuery } from "@tanstack/react-query";


function Inspection(
    {
        inspection
    }: {
        inspection: any
    }
) {
    function fixInspection(inspection: any) {
        return fileApiService.fixInspection(inspection)
    }

    return <li key={inspection.path} style={{ border: "1px solid black" }}>
        <div>
            <button
                onClick={() => fixInspection(inspection).then(it => alert(JSON.stringify(it)))}
            >
                Fix
            </button>
        </div>
        <br/>
        <span>{inspection.type}</span>
        <br/>
        <span>{inspection.description}</span>
        <br/>
        <span>{inspection.path}</span>
    </li>;
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

    return <>
        Inspections...
        <button onClick={() =>
            setInspectionsRequested(true)
        }>
            Load
        </button>
        <button onClick={() =>
            setInspectionsRequested(false)
        }>
            Close
        </button>
        <div>
            {!!inspections && inspections.length > 0
                ? <ul>
                    {inspections.map(inspection => {
                        return <Inspection key={inspection.path} inspection={inspection}/>
                    })}
                </ul>
                : <span> All fine </span>}
        </div>
    </>
}

export default React.memo(Inspections)

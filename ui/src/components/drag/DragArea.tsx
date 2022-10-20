import {ReactNode, useId} from "react";
import useDragSelect from "lib/react/hooks/useDragSelect";

export default function DragArea(
    {
        setSelectedItems,
        children,
    }: {
        setSelectedItems: (items: string[]) => void,
        children?: ReactNode,
    }
) {

    let id = useId()
    useDragSelect("drag-selectable", `dragarea${id}`,
        (items) => {
            setSelectedItems(items.filter((it, index, self) => self.indexOf(it) === index))
        }
    )

    return <div id={id} className={`dragarea${id}`}>{children}</div>
}

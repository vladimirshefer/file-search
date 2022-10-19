import {useEffect} from "react";
import DragSelect from "dragselect";

export default function useDragSelect(
        selectablesClassName: string,
        areaClassName: string,
        setSelectedIds: ((ids: string[]) => void)
) {

    function enableSelection(): () => void {
        try {
            let dragselect = new DragSelect({
                selectables: document.getElementsByClassName(selectablesClassName),
                area: document.getElementsByClassName(areaClassName)[0],
                draggability: false,
            } as unknown as Settings);

            let itemsSelected = function() {
                let selectedItems = dragselect.getSelection()
                    .map(it => it.getAttribute("data-selection-id"))
                    .filter(it => !!it) as string[];
                setSelectedIds(selectedItems);
                console.log(selectedItems);
            }
            dragselect.subscribe("elementselect", itemsSelected);
            dragselect.subscribe("elementunselect",itemsSelected);
            return function cleanup() {
                dragselect.stop()
            }
        } catch (e) {
            console.log(e)
        }
        return () => {};
    }

    useEffect(() => {
        return enableSelection()
    }, [])

}

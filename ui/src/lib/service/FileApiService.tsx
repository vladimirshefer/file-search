import axios from "axios";
import {MediaDirectoryInfo} from "lib/Api";

export default class FileApiService {

    async loadContent(filePath: string): Promise<MediaDirectoryInfo> {
        let response = await axios.get("/api/files/list", {
            params: {
                path: filePath
            }
        });

        return response.data
    }

    async loadInspections(filePath: string): Promise<any[]> {
        let response = await axios.get("/api/files/inspections", {
            params: {
                path: filePath
            }
        });

        return response.data
    }

    async fixInspection(inspection: any): Promise<any[]> {
        let response = await axios.post("/api/files/inspections/fix", inspection);

        return response.data
    }

    async loadStats(filePath: string) {
        let response = await axios.get("/api/files/stats", {
            params: {
                path: filePath
            }
        });

        return response.data;
    }

    async loadReadme(filePath: string): Promise<string | null> {
        let response = await axios.get("/api/files/readme", {
            params: {
                path: filePath
            }
        });

        return response.data?.content as string || null;
    }

    async optimize(basePath: string, paths: string[]) {
        try {
            await axios.post("/api/files/optimize", {
                basePath,
                paths
            });
        } catch (e) {
            alert("Could not init optimization")
        }
    }
}

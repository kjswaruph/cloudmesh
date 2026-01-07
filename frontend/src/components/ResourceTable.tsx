"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api-client";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { 
  Search, 
  Filter, 
  MoreHorizontal, 
  Play, 
  Square, 
  Trash2, 
  Edit,
  RefreshCw
} from "lucide-react";

interface CloudResource {
  id: string;
  name: string;
  type: string;
  provider: "AWS" | "Azure" | "GCP";
  region: string;
  status: "running" | "stopped" | "error" | "pending";
  cost: number;
  lastUpdated: string;
}

const getStatusColor = (status: string) => {
  switch (status) {
    case "running": return "bg-green-100 text-green-800";
    case "stopped": return "bg-gray-100 text-gray-800";
    case "error": return "bg-red-100 text-red-800";
    case "pending": return "bg-yellow-100 text-yellow-800";
    default: return "bg-gray-100 text-gray-800";
  }
};

const getProviderColor = (provider: string) => {
  switch (provider) {
    case "AWS": return "bg-orange-100 text-orange-800";
    case "Azure": return "bg-blue-100 text-blue-800";
    case "GCP": return "bg-green-100 text-green-800";
    default: return "bg-gray-100 text-gray-800";
  }
};

export function ResourceTable() {
  const [resources, setResources] = useState<CloudResource[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterProvider, setFilterProvider] = useState<string>("all");

  useEffect(() => {
    const fetchResources = async () => {
      try {
        // Fetch all projects and their resources
        const projects = await api.projects.list();
        
        const allResources: CloudResource[] = [];
        
        // Fetch resources for each project in parallel
        await Promise.all(
          projects.map(async (project: any) => {
            try {
              const projectResources = await api.projects.getResources(project.id);
              
              // Transform backend resource format to component format
              const transformedResources = projectResources.map((resource: any) => ({
                id: resource.id || resource.resourceId,
                name: resource.name || resource.resourceName || 'Unnamed Resource',
                type: resource.type || resource.resourceType || 'Unknown',
                provider: resource.provider || 'Unknown',
                region: resource.region || 'N/A',
                status: (resource.status?.toLowerCase() || 'unknown') as "running" | "stopped" | "error" | "pending",
                cost: resource.monthlyCost || resource.cost || 0,
                lastUpdated: resource.lastUpdated || resource.updatedAt || new Date().toISOString(),
              }));
              
              allResources.push(...transformedResources);
            } catch (err) {
              console.warn(`Failed to fetch resources for project ${project.id}:`, err);
            }
          })
        );
        
        setResources(allResources);
      } catch (err) {
        console.error('Error fetching resources:', err);
        setError(err instanceof Error ? err.message : 'Failed to load resources');
      } finally {
        setLoading(false);
      }
    };

    fetchResources();
  }, []);

  const handleResourceAction = async (resourceId: string, action: string) => {
    // TODO: Resource lifecycle management (start/stop/delete) requires backend implementation
    // These endpoints are not yet available in the API
    console.warn(`Resource action '${action}' on ${resourceId} - Backend API pending`);
    alert(`Resource management actions are not yet implemented in the backend API.\nAction: ${action}\nResource: ${resourceId}`);
  };

  const filteredResources = resources.filter((resource) => {
    const matchesSearch = resource.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         resource.type.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesProvider = filterProvider === "all" || resource.provider === filterProvider;
    return matchesSearch && matchesProvider;
  });

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-4 w-72" />
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="flex items-center space-x-4">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-4 w-16" />
                <Skeleton className="h-4 w-20" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="border-destructive/50">
        <CardHeader>
          <CardTitle className="text-destructive">Error Loading Resources</CardTitle>
          <CardDescription>{error}</CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Cloud Resources</CardTitle>
        <CardDescription>Manage your infrastructure across all cloud providers</CardDescription>
        
        {/* Search and Filter Controls */}
        <div className="flex items-center space-x-4 pt-4">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <Input
              placeholder="Search resources..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline">
                <Filter className="mr-2 h-4 w-4" />
                {filterProvider === "all" ? "All Providers" : filterProvider}
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              <DropdownMenuLabel>Filter by Provider</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={() => setFilterProvider("all")}>
                All Providers
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setFilterProvider("AWS")}>
                AWS
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setFilterProvider("Azure")}>
                Azure
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setFilterProvider("GCP")}>
                GCP
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          
          <Button variant="outline" onClick={() => window.location.reload()}>
            <RefreshCw className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Provider</TableHead>
              <TableHead>Region</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Monthly Cost</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredResources.map((resource) => (
              <TableRow key={resource.id}>
                <TableCell className="font-medium">{resource.name}</TableCell>
                <TableCell>{resource.type}</TableCell>
                <TableCell>
                  <Badge className={getProviderColor(resource.provider)}>
                    {resource.provider}
                  </Badge>
                </TableCell>
                <TableCell>{resource.region}</TableCell>
                <TableCell>
                  <Badge className={getStatusColor(resource.status)}>
                    {resource.status}
                  </Badge>
                </TableCell>
                <TableCell className="text-right font-mono">
                  ${resource.cost.toFixed(2)}
                </TableCell>
                <TableCell className="text-right">
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" className="h-8 w-8 p-0">
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuLabel>Actions</DropdownMenuLabel>
                      <DropdownMenuSeparator />
                      {resource.status === "stopped" ? (
                        <DropdownMenuItem onClick={() => handleResourceAction(resource.id, "start")}>
                          <Play className="mr-2 h-4 w-4" />
                          Start
                        </DropdownMenuItem>
                      ) : (
                        <DropdownMenuItem onClick={() => handleResourceAction(resource.id, "stop")}>
                          <Square className="mr-2 h-4 w-4" />
                          Stop
                        </DropdownMenuItem>
                      )}
                      <DropdownMenuItem onClick={() => handleResourceAction(resource.id, "edit")}>
                        <Edit className="mr-2 h-4 w-4" />
                        Edit
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem 
                        className="text-red-600"
                        onClick={() => handleResourceAction(resource.id, "delete")}
                      >
                        <Trash2 className="mr-2 h-4 w-4" />
                        Delete
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        
        {filteredResources.length === 0 && (
          <div className="text-center py-8 text-muted-foreground">
            No resources found matching your criteria.
          </div>
        )}
      </CardContent>
    </Card>
  );
}
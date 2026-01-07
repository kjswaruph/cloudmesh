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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { 
  Search, 
  Filter, 
  MoreHorizontal, 
  Play, 
  Square, 
  Trash2, 
  RefreshCw,
  AlertCircle,
  Server,
  Database,
  Cloud
} from "lucide-react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Alert, AlertDescription } from "@/components/ui/alert";
import Image from "next/image";

interface CloudResource {
  id: string;
  name: string;
  type: string;
  provider: "AWS" | "Azure" | "GCP" | "DigitalOcean";
  region: string;
  status: "running" | "stopped" | "error" | "pending";
  cost: number;
  lastUpdated: string;
  projectId?: string;
  projectName?: string;
}

const getStatusColor = (status: string) => {
  switch (status) {
    case "running": return "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300";
    case "stopped": return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    case "error": return "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300";
    case "pending": return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300";
    default: return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
  }
};

const providerIcons: Record<string, string> = {
  AWS: "/providers/aws.png",
  GCP: "/providers/gcp.png",
  Azure: "/providers/azure.png",
  DigitalOcean: "/providers/do.png"
};

const providerColors: Record<string, string> = {
  AWS: "bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-300",
  GCP: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300",
  Azure: "bg-sky-100 text-sky-800 dark:bg-sky-900 dark:text-sky-300",
  DigitalOcean: "bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-300"
};

export function ResourcesPanel() {
  const [resources, setResources] = useState<CloudResource[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterProvider, setFilterProvider] = useState<string>("all");
  const [filterStatus, setFilterStatus] = useState<string>("all");
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [resourceToDelete, setResourceToDelete] = useState<CloudResource | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const fetchResources = async () => {
    setLoading(true);
    setError(null);
    try {
      const projects = await api.projects.list();
      const allResources: CloudResource[] = [];
      
      await Promise.all(
        projects.map(async (project: any) => {
          try {
            const projectResources = await api.projects.getResources(project.id);
            
            const transformedResources = projectResources.map((resource: any) => ({
              id: resource.id || resource.resourceId,
              name: resource.name || resource.resourceName || 'Unnamed Resource',
              type: resource.type || resource.resourceType || 'Unknown',
              provider: resource.provider || 'Unknown',
              region: resource.region || 'N/A',
              status: (resource.status?.toLowerCase() || 'unknown') as "running" | "stopped" | "error" | "pending",
              cost: resource.monthlyCost || resource.cost || 0,
              lastUpdated: resource.lastUpdated || resource.updatedAt || new Date().toISOString(),
              projectId: project.id,
              projectName: project.name || 'Unnamed Project'
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

  useEffect(() => {
    fetchResources();
  }, []);

  const handleResourceAction = async (resource: CloudResource, action: string) => {
    setActionLoading(`${resource.id}-${action}`);
    try {
      // TODO: Implement actual backend API calls when available
      console.log(`Action '${action}' on resource ${resource.id}`);
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      alert(`Resource management actions are not yet fully implemented in the backend API.\nAction: ${action}\nResource: ${resource.name}`);
      
      // Refresh resources after action
      await fetchResources();
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to ${action} resource`);
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeleteResource = async () => {
    if (!resourceToDelete) return;
    
    setActionLoading(`${resourceToDelete.id}-delete`);
    try {
      // TODO: Implement actual delete API call
      console.log(`Deleting resource ${resourceToDelete.id}`);
      
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Remove from local state
      setResources(prev => prev.filter(r => r.id !== resourceToDelete.id));
      setDeleteDialogOpen(false);
      setResourceToDelete(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete resource');
    } finally {
      setActionLoading(null);
    }
  };

  const filteredResources = resources.filter((resource) => {
    const matchesSearch = 
      resource.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      resource.type.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (resource.projectName?.toLowerCase() || '').includes(searchTerm.toLowerCase());
    const matchesProvider = filterProvider === "all" || resource.provider === filterProvider;
    const matchesStatus = filterStatus === "all" || resource.status === filterStatus;
    return matchesSearch && matchesProvider && matchesStatus;
  });

  const totalCost = filteredResources.reduce((sum, r) => sum + r.cost, 0);
  const resourcesByStatus = {
    running: filteredResources.filter(r => r.status === 'running').length,
    stopped: filteredResources.filter(r => r.status === 'stopped').length,
    error: filteredResources.filter(r => r.status === 'error').length,
    pending: filteredResources.filter(r => r.status === 'pending').length
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
            <Skeleton className="h-4 w-72" />
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Resources</CardTitle>
            <Server className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{filteredResources.length}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Running</CardTitle>
            <div className="h-2 w-2 rounded-full bg-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{resourcesByStatus.running}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Stopped</CardTitle>
            <div className="h-2 w-2 rounded-full bg-gray-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{resourcesByStatus.stopped}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Monthly Cost</CardTitle>
            <Database className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${totalCost.toFixed(2)}</div>
          </CardContent>
        </Card>
      </div>

      {/* Resources Table */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Cloud Resources</CardTitle>
              <CardDescription>Manage your infrastructure across all cloud providers</CardDescription>
            </div>
            <Button variant="outline" size="sm" onClick={fetchResources}>
              <RefreshCw className="h-4 w-4 mr-2" />
              Refresh
            </Button>
          </div>
          
          {/* Search and Filter Controls */}
          <div className="flex flex-col sm:flex-row gap-4 pt-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
              <Input
                placeholder="Search resources, types, or projects..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            
            <Select value={filterProvider} onValueChange={setFilterProvider}>
              <SelectTrigger className="w-full sm:w-[180px]">
                <Filter className="h-4 w-4 mr-2" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Providers</SelectItem>
                <SelectItem value="AWS">AWS</SelectItem>
                <SelectItem value="GCP">GCP</SelectItem>
                <SelectItem value="Azure">Azure</SelectItem>
                <SelectItem value="DigitalOcean">DigitalOcean</SelectItem>
              </SelectContent>
            </Select>

            <Select value={filterStatus} onValueChange={setFilterStatus}>
              <SelectTrigger className="w-full sm:w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="running">Running</SelectItem>
                <SelectItem value="stopped">Stopped</SelectItem>
                <SelectItem value="error">Error</SelectItem>
                <SelectItem value="pending">Pending</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        
        <CardContent>
          {filteredResources.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Cloud className="h-12 w-12 text-muted-foreground mb-4" />
              <p className="text-lg font-medium">No resources found</p>
              <p className="text-sm text-muted-foreground mt-1">
                {searchTerm || filterProvider !== "all" || filterStatus !== "all"
                  ? "Try adjusting your filters"
                  : "Resources will appear here once you start deploying infrastructure"}
              </p>
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Resource</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Provider</TableHead>
                    <TableHead>Region</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Project</TableHead>
                    <TableHead className="text-right">Monthly Cost</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredResources.map((resource) => (
                    <TableRow key={resource.id}>
                      <TableCell className="font-medium">{resource.name}</TableCell>
                      <TableCell>
                        <span className="text-sm text-muted-foreground">{resource.type}</span>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Image 
                            src={providerIcons[resource.provider] || providerIcons.AWS} 
                            alt={resource.provider}
                            width={20}
                            height={20}
                            className="object-contain"
                          />
                          <Badge className={providerColors[resource.provider]}>
                            {resource.provider}
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm">{resource.region}</span>
                      </TableCell>
                      <TableCell>
                        <Badge className={getStatusColor(resource.status)}>
                          {resource.status}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm text-muted-foreground">
                          {resource.projectName || 'N/A'}
                        </span>
                      </TableCell>
                      <TableCell className="text-right font-mono">
                        ${resource.cost.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button 
                              variant="ghost" 
                              className="h-8 w-8 p-0"
                              disabled={!!actionLoading}
                            >
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuLabel>Actions</DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            {resource.status === "stopped" ? (
                              <DropdownMenuItem 
                                onClick={() => handleResourceAction(resource, "start")}
                                disabled={actionLoading === `${resource.id}-start`}
                              >
                                <Play className="mr-2 h-4 w-4" />
                                Start
                              </DropdownMenuItem>
                            ) : (
                              <DropdownMenuItem 
                                onClick={() => handleResourceAction(resource, "stop")}
                                disabled={actionLoading === `${resource.id}-stop`}
                              >
                                <Square className="mr-2 h-4 w-4" />
                                Stop
                              </DropdownMenuItem>
                            )}
                            <DropdownMenuSeparator />
                            <DropdownMenuItem 
                              className="text-red-600"
                              onClick={() => {
                                setResourceToDelete(resource);
                                setDeleteDialogOpen(true);
                              }}
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
            </div>
          )}
        </CardContent>
      </Card>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Resource</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete <strong>{resourceToDelete?.name}</strong>?
              This action cannot be undone and will permanently remove the resource from your infrastructure.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setResourceToDelete(null)}>
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleDeleteResource}
              className="bg-red-600 hover:bg-red-700"
              disabled={!!actionLoading}
            >
              {actionLoading === `${resourceToDelete?.id}-delete` ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

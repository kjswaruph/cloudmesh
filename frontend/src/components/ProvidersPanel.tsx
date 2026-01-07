"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
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
import { api } from "@/lib/api-client";
import { Cloud, Trash2, CheckCircle, AlertCircle, Loader2, Plus } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import Image from "next/image";

interface Credential {
  id: string;
  provider: string;
  friendlyName: string;
  region?: string;
  status?: string;
  createdAt?: string;
  validated?: boolean;
}

type Provider = "AWS" | "GCP" | "Azure" | "DigitalOcean";

const providerColors: Record<Provider, string> = {
  AWS: "bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-300",
  GCP: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300",
  Azure: "bg-sky-100 text-sky-800 dark:bg-sky-900 dark:text-sky-300",
  DigitalOcean: "bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-300"
};

const providerIcons: Record<Provider, string> = {
  AWS: "/providers/aws.png",
  GCP: "/providers/gcp.png",
  Azure: "/providers/azure.png",
  DigitalOcean: "/providers/do.png"
};

export function ProvidersPanel() {
  const [credentials, setCredentials] = useState<Credential[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedProvider, setSelectedProvider] = useState<Provider | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [credentialToDelete, setCredentialToDelete] = useState<string | null>(null);
  const [validating, setValidating] = useState<string | null>(null);

  // Form states for each provider
  const [awsForm, setAwsForm] = useState({
    friendlyName: "",
    roleArn: "",
    externalId: "",
    region: "us-east-1"
  });

  const [gcpForm, setGcpForm] = useState({
    friendlyName: "",
    serviceAccountJson: "",
    projectId: "",
    region: "us-central1"
  });

  const [azureForm, setAzureForm] = useState({
    friendlyName: "",
    clientId: "",
    clientSecret: "",
    tenantId: "",
    subscriptionId: "",
    region: "eastus"
  });

  const [doForm, setDoForm] = useState({
    friendlyName: "",
    apiToken: "",
    region: "nyc3"
  });

  const fetchCredentials = async () => {
    try {
      const data = await api.credentials.list();
      setCredentials(data);
      setError(null);
    } catch (err) {
      console.error("Error fetching credentials:", err);
      setError(err instanceof Error ? err.message : "Failed to load credentials");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCredentials();
  }, []);

  const handleConnect = (provider: Provider) => {
    setSelectedProvider(provider);
    setIsDialogOpen(true);
  };

  const resetForms = () => {
    setAwsForm({ friendlyName: "", roleArn: "", externalId: "", region: "us-east-1" });
    setGcpForm({ friendlyName: "", serviceAccountJson: "", projectId: "", region: "us-central1" });
    setAzureForm({ friendlyName: "", clientId: "", clientSecret: "", tenantId: "", subscriptionId: "", region: "eastus" });
    setDoForm({ friendlyName: "", apiToken: "", region: "nyc3" });
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    try {
      switch (selectedProvider) {
        case "AWS":
          await api.credentials.connectAWS(awsForm);
          break;
        case "GCP":
          await api.credentials.connectGCP(gcpForm);
          break;
        case "Azure":
          await api.credentials.connectAzure(azureForm);
          break;
        case "DigitalOcean":
          await api.credentials.connectDigitalOcean(doForm);
          break;
      }
      await fetchCredentials();
      setIsDialogOpen(false);
      resetForms();
    } catch (err) {
      console.error("Error connecting provider:", err);
      setError(err instanceof Error ? err.message : "Failed to connect provider");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!credentialToDelete) return;
    try {
      await api.credentials.delete(credentialToDelete);
      await fetchCredentials();
      setDeleteDialogOpen(false);
      setCredentialToDelete(null);
    } catch (err) {
      console.error("Error deleting credential:", err);
      setError(err instanceof Error ? err.message : "Failed to delete credential");
    }
  };

  const handleValidate = async (credentialId: string) => {
    setValidating(credentialId);
    try {
      // TODO: Update credential status after validation
      await fetch(`${process.env.NEXT_PUBLIC_API_BASE || ''}/api/credentials/${credentialId}/validate`, {
        method: 'POST',
        credentials: 'include',
      });
      await fetchCredentials();
    } catch (err) {
      console.error("Error validating credential:", err);
    } finally {
      setValidating(null);
    }
  };

  const renderProviderForm = () => {
    switch (selectedProvider) {
      case "AWS":
        return (
          <div className="space-y-6">
            <div>
              <Label htmlFor="aws-name">Friendly Name</Label>
              <Input
                id="aws-name"
                placeholder="My AWS Account"
                value={awsForm.friendlyName}
                onChange={(e) => setAwsForm({ ...awsForm, friendlyName: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="aws-role">Role ARN</Label>
              <Input
                id="aws-role"
                placeholder="arn:aws:iam::123456789012:role/MyRole"
                value={awsForm.roleArn}
                onChange={(e) => setAwsForm({ ...awsForm, roleArn: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="aws-external">External ID</Label>
              <Input
                id="aws-external"
                placeholder="ext-123"
                value={awsForm.externalId}
                onChange={(e) => setAwsForm({ ...awsForm, externalId: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="aws-region">Region</Label>
              <Select value={awsForm.region} onValueChange={(value: string) => setAwsForm({ ...awsForm, region: value })}>
                <SelectTrigger className="mt-2">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="us-east-1">US East (N. Virginia)</SelectItem>
                  <SelectItem value="us-west-2">US West (Oregon)</SelectItem>
                  <SelectItem value="eu-west-1">EU (Ireland)</SelectItem>
                  <SelectItem value="ap-southeast-1">Asia Pacific (Singapore)</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        );

      case "GCP":
        return (
          <div className="space-y-6">
            <div>
              <Label htmlFor="gcp-name">Friendly Name</Label>
              <Input
                id="gcp-name"
                placeholder="My GCP Project"
                value={gcpForm.friendlyName}
                onChange={(e) => setGcpForm({ ...gcpForm, friendlyName: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="gcp-project">Project ID</Label>
              <Input
                id="gcp-project"
                placeholder="my-project-123"
                value={gcpForm.projectId}
                onChange={(e) => setGcpForm({ ...gcpForm, projectId: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="gcp-json">Service Account JSON</Label>
              <Textarea
                id="gcp-json"
                placeholder='{"type": "service_account", ...}'
                className="font-mono text-xs mt-2"
                rows={6}
                value={gcpForm.serviceAccountJson}
                onChange={(e) => setGcpForm({ ...gcpForm, serviceAccountJson: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="gcp-region">Region</Label>
              <Select value={gcpForm.region} onValueChange={(value: string) => setGcpForm({ ...gcpForm, region: value })}>
                <SelectTrigger className="mt-2">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="us-central1">US Central 1</SelectItem>
                  <SelectItem value="us-east1">US East 1</SelectItem>
                  <SelectItem value="europe-west1">Europe West 1</SelectItem>
                  <SelectItem value="asia-southeast1">Asia Southeast 1</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        );

      case "Azure":
        return (
          <div className="space-y-6">
            <div>
              <Label htmlFor="azure-name">Friendly Name</Label>
              <Input
                id="azure-name"
                placeholder="My Azure Subscription"
                value={azureForm.friendlyName}
                onChange={(e) => setAzureForm({ ...azureForm, friendlyName: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="azure-client">Client ID</Label>
              <Input
                id="azure-client"
                placeholder="11111111-1111-1111-1111-111111111111"
                value={azureForm.clientId}
                onChange={(e) => setAzureForm({ ...azureForm, clientId: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="azure-secret">Client Secret</Label>
              <Input
                id="azure-secret"
                type="password"
                placeholder="Enter client secret"
                value={azureForm.clientSecret}
                onChange={(e) => setAzureForm({ ...azureForm, clientSecret: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="azure-tenant">Tenant ID</Label>
              <Input
                id="azure-tenant"
                placeholder="22222222-2222-2222-2222-222222222222"
                value={azureForm.tenantId}
                onChange={(e) => setAzureForm({ ...azureForm, tenantId: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="azure-sub">Subscription ID</Label>
              <Input
                id="azure-sub"
                placeholder="33333333-3333-3333-3333-333333333333"
                value={azureForm.subscriptionId}
                onChange={(e) => setAzureForm({ ...azureForm, subscriptionId: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="azure-region">Region</Label>
              <Select value={azureForm.region} onValueChange={(value: string) => setAzureForm({ ...azureForm, region: value })}>
                <SelectTrigger className="mt-2">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="eastus">East US</SelectItem>
                  <SelectItem value="westus">West US</SelectItem>
                  <SelectItem value="westeurope">West Europe</SelectItem>
                  <SelectItem value="southeastasia">Southeast Asia</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        );

      case "DigitalOcean":
        return (
          <div className="space-y-6">
            <div>
              <Label htmlFor="do-name">Friendly Name</Label>
              <Input
                id="do-name"
                placeholder="My DigitalOcean Account"
                value={doForm.friendlyName}
                onChange={(e) => setDoForm({ ...doForm, friendlyName: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="do-token">API Token</Label>
              <Input
                id="do-token"
                type="password"
                placeholder="Enter your DigitalOcean API token"
                value={doForm.apiToken}
                onChange={(e) => setDoForm({ ...doForm, apiToken: e.target.value })}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="do-region">Region</Label>
              <Select value={doForm.region} onValueChange={(value: string) => setDoForm({ ...doForm, region: value })}>
                <SelectTrigger className="mt-2">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="nyc3">New York 3</SelectItem>
                  <SelectItem value="sfo3">San Francisco 3</SelectItem>
                  <SelectItem value="lon1">London 1</SelectItem>
                  <SelectItem value="sgp1">Singapore 1</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-4 w-72" />
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-24 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Cloud Providers</CardTitle>
          <CardDescription>
            Connect and manage your cloud provider accounts
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {/* Provider Connection Buttons */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {(["AWS", "GCP", "Azure", "DigitalOcean"] as Provider[]).map((provider) => (
              <Dialog
                key={provider}
                open={isDialogOpen && selectedProvider === provider}
                onOpenChange={(open) => {
                  setIsDialogOpen(open);
                  if (!open) {
                    setSelectedProvider(null);
                    resetForms();
                  }
                }}
              >
                <DialogTrigger asChild>
                  <Button
                    variant="outline"
                    className="h-24 flex flex-col items-center justify-center gap-2"
                    onClick={() => handleConnect(provider)}
                  >
                    <Image 
                      src={providerIcons[provider]} 
                      alt={`${provider} logo`}
                      width={48}
                      height={48}
                      className="object-contain"
                    />
                    <span className="font-medium">{provider}</span>
                  </Button>
                </DialogTrigger>
                <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                  <DialogHeader>
                    <DialogTitle>Connect {provider}</DialogTitle>
                    <DialogDescription>
                      Enter your {provider} credentials to connect your account
                    </DialogDescription>
                  </DialogHeader>
                  {renderProviderForm()}
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setIsDialogOpen(false)} disabled={isSubmitting}>
                      Cancel
                    </Button>
                    <Button onClick={handleSubmit} disabled={isSubmitting}>
                      {isSubmitting ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Connecting...
                        </>
                      ) : (
                        <>
                          <Plus className="mr-2 h-4 w-4" />
                          Connect
                        </>
                      )}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            ))}
          </div>

          {/* Connected Credentials List */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold">Connected Accounts</h3>
            {credentials.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                <Cloud className="mx-auto h-12 w-12 mb-4 opacity-50" />
                <p>No cloud providers connected yet</p>
                <p className="text-sm">Connect your first provider to get started</p>
              </div>
            ) : (
              <div className="grid gap-4">
                {credentials.map((cred) => (
                  <Card key={cred.id}>
                    <CardContent className="flex items-center justify-between p-4">
                      <div className="flex items-center gap-4">
                        <Image 
                          src={providerIcons[cred.provider as Provider]} 
                          alt={`${cred.provider} logo`}
                          width={40}
                          height={40}
                          className="object-contain"
                        />
                        <div>
                          <div className="flex items-center gap-2">
                            <p className="font-medium">{cred.friendlyName}</p>
                            <Badge className={providerColors[cred.provider as Provider]}>
                              {cred.provider}
                            </Badge>
                          </div>
                          <p className="text-sm text-muted-foreground">
                            Region: {cred.region || "N/A"}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleValidate(cred.id)}
                          disabled={validating === cred.id}
                        >
                          {validating === cred.id ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                          ) : (
                            <CheckCircle className="h-4 w-4" />
                          )}
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => {
                            setCredentialToDelete(cred.id);
                            setDeleteDialogOpen(true);
                          }}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Credential</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this credential? This action cannot be undone and will
              remove access to all resources from this provider.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground">
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

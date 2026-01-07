"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api-client";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Alert, AlertDescription } from "@/components/ui/alert";
import {
  DollarSign,
  TrendingUp,
  TrendingDown,
  Calendar,
  Download,
  AlertCircle,
  CreditCard,
  BarChart3,
} from "lucide-react";
import { AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import Image from "next/image";

interface BillingData {
  currentMonth: number;
  previousMonth: number;
  yearToDate: number;
  projected: number;
}

interface CostByProvider {
  provider: string;
  cost: number;
  change: number;
}

interface Invoice {
  id: string;
  date: string;
  amount: number;
  status: "paid" | "pending" | "overdue";
  provider: string;
}

interface CostTrend {
  date: string;
  total: number;
  aws?: number;
  gcp?: number;
  azure?: number;
  digitalocean?: number;
}

const providerColors: Record<string, string> = {
  AWS: "#FF9900",
  GCP: "#4285F4",
  Azure: "#0089D6",
  DigitalOcean: "#0080FF"
};

const providerIcons: Record<string, string> = {
  AWS: "/providers/aws.png",
  GCP: "/providers/gcp.png",
  Azure: "/providers/azure.png",
  DigitalOcean: "/providers/do.png"
};

export function BillingPanel() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [billingData, setBillingData] = useState<BillingData | null>(null);
  const [costByProvider, setCostByProvider] = useState<CostByProvider[]>([]);
  const [costTrends, setCostTrends] = useState<CostTrend[]>([]);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [selectedPeriod, setSelectedPeriod] = useState("30");

  useEffect(() => {
    fetchBillingData();
  }, [selectedPeriod]);

  const fetchBillingData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Fetch credentials and their costs
      const credentials = await api.credentials.list();
      
      // Calculate current month costs
      const now = new Date();
      const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
      const lastDayOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);
      
      const costPromises = credentials.map(async (cred: any) => {
        try {
          const costs = await api.costs.getCredentialCosts(
            cred.id,
            firstDayOfMonth.toISOString().split('T')[0],
            lastDayOfMonth.toISOString().split('T')[0]
          );
          return {
            provider: cred.provider,
            credentialId: cred.id,
            cost: costs.reduce((sum: number, c: any) => sum + (c.totalCost || 0), 0)
          };
        } catch {
          return { provider: cred.provider, credentialId: cred.id, cost: 0 };
        }
      });

      const providerCosts = await Promise.all(costPromises);
      
      // Aggregate by provider
      const aggregated: Record<string, number> = {};
      providerCosts.forEach(pc => {
        aggregated[pc.provider] = (aggregated[pc.provider] || 0) + pc.cost;
      });

      const currentTotal = Object.values(aggregated).reduce((sum, val) => sum + val, 0);

      // Get previous month data
      const firstDayPrevMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
      const lastDayPrevMonth = new Date(now.getFullYear(), now.getMonth(), 0);
      
      const prevCostPromises = credentials.map(async (cred: any) => {
        try {
          const costs = await api.costs.getCredentialCosts(
            cred.id,
            firstDayPrevMonth.toISOString().split('T')[0],
            lastDayPrevMonth.toISOString().split('T')[0]
          );
          return costs.reduce((sum: number, c: any) => sum + (c.totalCost || 0), 0);
        } catch {
          return 0;
        }
      });

      const prevCosts = await Promise.all(prevCostPromises);
      const previousTotal = prevCosts.reduce((sum, val) => sum + val, 0);

      // Calculate year to date
      const firstDayOfYear = new Date(now.getFullYear(), 0, 1);
      const ytdCostPromises = credentials.map(async (cred: any) => {
        try {
          const costs = await api.costs.getCredentialCosts(
            cred.id,
            firstDayOfYear.toISOString().split('T')[0],
            now.toISOString().split('T')[0]
          );
          return costs.reduce((sum: number, c: any) => sum + (c.totalCost || 0), 0);
        } catch {
          return 0;
        }
      });

      const ytdCosts = await Promise.all(ytdCostPromises);
      const ytdTotal = ytdCosts.reduce((sum, val) => sum + val, 0);

      setBillingData({
        currentMonth: currentTotal,
        previousMonth: previousTotal,
        yearToDate: ytdTotal,
        projected: currentTotal * (30 / now.getDate()) // Simple projection
      });

      // Set cost by provider with change percentage
      const providerData: CostByProvider[] = Object.entries(aggregated).map(([provider, cost]) => ({
        provider,
        cost,
        change: previousTotal > 0 ? ((cost - previousTotal) / previousTotal) * 100 : 0
      }));
      setCostByProvider(providerData);

      // Generate cost trends for the last 6 months
      const trends: CostTrend[] = [];
      for (let i = 5; i >= 0; i--) {
        const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
        const endDate = new Date(now.getFullYear(), now.getMonth() - i + 1, 0);
        
        const monthCosts = await Promise.all(
          credentials.map(async (cred: any) => {
            try {
              const costs = await api.costs.getCredentialCosts(
                cred.id,
                date.toISOString().split('T')[0],
                endDate.toISOString().split('T')[0]
              );
              return {
                provider: cred.provider.toLowerCase(),
                cost: costs.reduce((sum: number, c: any) => sum + (c.totalCost || 0), 0)
              };
            } catch {
              return { provider: cred.provider.toLowerCase(), cost: 0 };
            }
          })
        );

        const monthData: any = {
          date: date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' }),
          total: 0
        };

        monthCosts.forEach(mc => {
          monthData[mc.provider] = mc.cost;
          monthData.total += mc.cost;
        });

        trends.push(monthData);
      }
      setCostTrends(trends);

      // Mock invoices for now (replace with real API when available)
      setInvoices([
        {
          id: "INV-2026-001",
          date: "2026-01-01",
          amount: currentTotal,
          status: "pending",
          provider: "All Providers"
        }
      ]);

    } catch (err) {
      console.error('Error fetching billing data:', err);
      setError(err instanceof Error ? err.message : 'Failed to load billing data');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i}>
            <CardHeader>
              <Skeleton className="h-6 w-48" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-64 w-full" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    );
  }

  const percentChange = billingData && billingData.previousMonth > 0
    ? ((billingData.currentMonth - billingData.previousMonth) / billingData.previousMonth) * 100
    : 0;

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Current Month</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${billingData?.currentMonth.toFixed(2)}</div>
            <div className="flex items-center text-xs text-muted-foreground mt-1">
              {percentChange >= 0 ? (
                <>
                  <TrendingUp className="h-3 w-3 mr-1 text-red-500" />
                  <span className="text-red-500">+{percentChange.toFixed(1)}%</span>
                </>
              ) : (
                <>
                  <TrendingDown className="h-3 w-3 mr-1 text-green-500" />
                  <span className="text-green-500">{percentChange.toFixed(1)}%</span>
                </>
              )}
              <span className="ml-1">from last month</span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Previous Month</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${billingData?.previousMonth.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Last billing cycle
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Year to Date</CardTitle>
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${billingData?.yearToDate.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground mt-1">
              {new Date().getFullYear()} total spend
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Projected (Month)</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${billingData?.projected.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Estimated end of month
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Cost by Provider */}
      <Card>
        <CardHeader>
          <CardTitle>Cost by Provider</CardTitle>
          <CardDescription>Breakdown of spending across cloud platforms</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {costByProvider.map((item) => (
              <div key={item.provider} className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Image
                    src={providerIcons[item.provider] || providerIcons.AWS}
                    alt={item.provider}
                    width={32}
                    height={32}
                    className="object-contain"
                  />
                  <div>
                    <p className="font-medium">{item.provider}</p>
                    <p className="text-sm text-muted-foreground">
                      {item.change >= 0 ? '+' : ''}{item.change.toFixed(1)}% from last month
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-bold text-lg">${item.cost.toFixed(2)}</p>
                  <p className="text-sm text-muted-foreground">
                    {billingData ? ((item.cost / billingData.currentMonth) * 100).toFixed(1) : 0}%
                  </p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Cost Trends */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Cost Trends</CardTitle>
              <CardDescription>6-month spending history</CardDescription>
            </div>
            <Select value={selectedPeriod} onValueChange={setSelectedPeriod}>
              <SelectTrigger className="w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="7">Last 7 days</SelectItem>
                <SelectItem value="30">Last 30 days</SelectItem>
                <SelectItem value="90">Last 90 days</SelectItem>
                <SelectItem value="180">Last 6 months</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={costTrends}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              {costByProvider.map((item) => (
                <Area
                  key={item.provider}
                  type="monotone"
                  dataKey={item.provider.toLowerCase()}
                  stackId="1"
                  stroke={providerColors[item.provider]}
                  fill={providerColors[item.provider]}
                  name={item.provider}
                />
              ))}
            </AreaChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Invoices */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Recent Invoices</CardTitle>
              <CardDescription>View and download your billing statements</CardDescription>
            </div>
            <Button variant="outline" size="sm">
              <Download className="h-4 w-4 mr-2" />
              Export All
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Invoice ID</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Provider</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {invoices.map((invoice) => (
                <TableRow key={invoice.id}>
                  <TableCell className="font-medium">{invoice.id}</TableCell>
                  <TableCell>{new Date(invoice.date).toLocaleDateString()}</TableCell>
                  <TableCell>{invoice.provider}</TableCell>
                  <TableCell>${invoice.amount.toFixed(2)}</TableCell>
                  <TableCell>
                    <Badge
                      variant={
                        invoice.status === "paid"
                          ? "default"
                          : invoice.status === "pending"
                          ? "secondary"
                          : "destructive"
                      }
                    >
                      {invoice.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm">
                      <Download className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}

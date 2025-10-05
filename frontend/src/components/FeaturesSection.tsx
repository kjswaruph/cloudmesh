import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function FeaturesSection() {
  return (
    <section id="features" className="w-full max-w-6xl mx-auto py-24 px-4">
      <h2 className="text-4xl font-bold mb-12 text-center">Features</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <Card>
          <CardHeader>
            <CardTitle>Unified Dashboard</CardTitle>
          </CardHeader>
          <CardContent>
            Manage AWS, Azure, GCP, and more from a single pane of glass.
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Automated Workflows</CardTitle>
          </CardHeader>
          <CardContent>
            Orchestrate multi-cloud deployments and automate routine tasks.
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Cost Optimization</CardTitle>
          </CardHeader>
          <CardContent>
            Analyze and optimize your cloud spend across providers.
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Project-Based Organization</CardTitle>
          </CardHeader>
          <CardContent>
            Organize resources and teams by project for better management and visibility.
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Real Time Monitoring</CardTitle>
          </CardHeader>
          <CardContent>
            Get instant insights and alerts on your cloud infrastructure health and usage.
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Enterprise Security</CardTitle>
          </CardHeader>
          <CardContent>
            Advanced security controls and compliance for enterprise-grade protection.
          </CardContent>
        </Card>
      </div>
    </section>
  );
}

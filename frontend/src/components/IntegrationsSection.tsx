import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function IntegrationsSection() {
  return (
    <section id="integrations" className="w-full max-w-6xl mx-auto py-24 px-4">
      <h2 className="text-4xl font-bold mb-12 text-center">Integrations</h2>
      <Tabs defaultValue="aws" className="w-full">
        <TabsList className="flex justify-center gap-4 mb-8">
          <TabsTrigger value="aws">AWS</TabsTrigger>
          <TabsTrigger value="azure">Azure</TabsTrigger>
          <TabsTrigger value="gcp">GCP</TabsTrigger>
        </TabsList>
        <TabsContent value="aws">
          <Card>
            <CardHeader>
              <CardTitle>AWS Integration</CardTitle>
            </CardHeader>
            <CardContent>
              Connect and manage your AWS resources seamlessly.
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="azure">
          <Card>
            <CardHeader>
              <CardTitle>Azure Integration</CardTitle>
            </CardHeader>
            <CardContent>
              Full support for Azure services and automation.
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="gcp">
          <Card>
            <CardHeader>
              <CardTitle>GCP Integration</CardTitle>
            </CardHeader>
            <CardContent>
              Manage Google Cloud resources with ease.
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </section>
  );
}

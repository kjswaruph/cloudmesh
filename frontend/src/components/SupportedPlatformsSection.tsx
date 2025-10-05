import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function SupportedPlatformsSection() {
  return (
    <section id="supported-platforms" className="w-full max-w-6xl mx-auto py-24 px-4">
      <h2 className="text-4xl font-bold mb-12 text-center">Supported Platforms</h2>
      <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
        <Card>
          <CardHeader>
            <CardTitle>AWS</CardTitle>
          </CardHeader>
          <CardContent>
            Seamless integration with Amazon Web Services for compute, storage, and more.
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Azure</CardTitle>
          </CardHeader>
          <CardContent>
            Full support for Microsoft Azure cloud services and automation.
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>GCP</CardTitle>
          </CardHeader>
          <CardContent>
            Manage Google Cloud Platform resources with ease and flexibility.
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Digital Ocean</CardTitle>
          </CardHeader>
          <CardContent>
            Integrate and manage Digital Ocean droplets, storage, and networking.
          </CardContent>
        </Card>
      </div>
    </section>
  );
}

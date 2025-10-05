import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Avatar } from "@/components/ui/avatar";

export function TestimonialsSection() {
  return (
    <section id="testimonials" className="w-full max-w-5xl mx-auto py-24 px-4">
      <h2 className="text-4xl font-bold mb-12 text-center">What Our Users Say</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <Card>
          <CardHeader className="flex flex-col items-center">
            <Avatar className="w-14 h-14 mb-2 bg-primary text-white">A</Avatar>
            <CardTitle className="text-center">Alex Johnson</CardTitle>
          </CardHeader>
          <CardContent className="text-center text-muted-foreground">
            “Cloud Mesh made our multi-cloud management effortless. The unified dashboard is a game changer!”
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-col items-center">
            <Avatar className="w-14 h-14 mb-2 bg-primary text-white">S</Avatar>
            <CardTitle className="text-center">Samantha Lee</CardTitle>
          </CardHeader>
          <CardContent className="text-center text-muted-foreground">
            “The real-time monitoring and automation features have saved us countless hours every week.”
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-col items-center">
            <Avatar className="w-14 h-14 mb-2 bg-primary text-white">M</Avatar>
            <CardTitle className="text-center">Michael Chen</CardTitle>
          </CardHeader>
          <CardContent className="text-center text-muted-foreground">
            “Enterprise security and cost optimization in one place. Highly recommended!”
          </CardContent>
        </Card>
      </div>
    </section>
  );
}

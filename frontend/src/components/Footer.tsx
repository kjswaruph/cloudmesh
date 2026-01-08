import { Separator } from "@/components/ui/separator";

export function Footer() {
  return (
    <footer className="w-full bg-background border-t border-border py-8 mt-24">
      <div className="max-w-6xl mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="text-muted-foreground text-sm">© {new Date().getFullYear()} Cloud Mesh. All rights reserved.</div>
        <Separator className="my-4 md:hidden" />
        <div className="flex gap-6 text-sm">
          <a href="#features" className="hover:underline">Features</a>
          <a href="#supported-platforms" className="hover:underline">Supported Platforms</a>
          {/* <a href="#faq" className="hover:underline">FAQ</a> */}
          {/* <a href="#testimonials" className="hover:underline">Testimonials</a> */}
        </div>
      </div>
    </footer>
  );
}

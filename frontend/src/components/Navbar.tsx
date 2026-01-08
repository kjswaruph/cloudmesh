import Link from "next/link";
import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/theme-toggle";
import { NavigationMenu, NavigationMenuList, NavigationMenuItem, NavigationMenuLink } from "@/components/ui/navigation-menu";

export function Navbar() {
  return (
    <nav className="w-full flex items-center justify-between py-4 px-8 bg-background/80 backdrop-blur-md border-b border-border sticky top-0 z-50">
      {/* Logo */}
      <div className="flex items-center gap-2">
        {/*<Avatar className="w-10 h-10 bg-primary text-white">*/}
        {/*  <span className="text-lg font-bold">CM</span>*/}
        {/*</Avatar>*/}
        <span className="font-bold text-xl tracking-tight">CloudMesh</span>
      </div>
      {/* Primary Nav */}
      <NavigationMenu className="hidden md:flex">
        <NavigationMenuList className="flex gap-6">
          <NavigationMenuItem>
            <NavigationMenuLink href="#features" className="text-base font-medium hover:text-primary transition-colors">Features</NavigationMenuLink>
          </NavigationMenuItem>
          <NavigationMenuItem>
            <NavigationMenuLink href="#supported-platforms" className="text-base font-medium hover:text-primary transition-colors">Platforms</NavigationMenuLink>
          </NavigationMenuItem>
          <NavigationMenuItem>
            <NavigationMenuLink href="#supported-platforms" className="text-base font-medium hover:text-primary transition-colors">Integrations</NavigationMenuLink>
          </NavigationMenuItem>
          {/* <NavigationMenuItem>
            <NavigationMenuLink href="#docs" className="text-base font-medium hover:text-primary transition-colors">Docs</NavigationMenuLink>
          </NavigationMenuItem> */}
        </NavigationMenuList>
      </NavigationMenu>
      {/* CTA Buttons */}
      <div className="flex gap-2 items-center">
        <ThemeToggle />
        <Button className="px-6" variant="default" asChild>
          <Link href="/signup">Get Started</Link>
        </Button>
        <Button className="px-6" variant="secondary" asChild>
          <Link href="/login">Login</Link>
        </Button>
      </div>
    </nav>
  );
}

"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import api from "@/lib/api-client";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
  SidebarFooter,
} from "@/components/ui/sidebar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Command,
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import { ThemeToggle } from "@/components/theme-toggle";
import {
  Home,
  FolderOpen,
  Server,
  CreditCard,
  Activity,
  Cloud,
  Settings,
  HelpCircle,
  User,
  LogOut,
  Search,
  Bell,
  Menu,
} from "lucide-react";

const primaryMenuItems = [
  {
    title: "Overview",
    url: "/dashboard",
    icon: Home,
  },
  {
    title: "Projects",
    url: "/projects",
    icon: FolderOpen,
  },
  {
    title: "Resources",
    url: "/dashboard/resources",
    icon: Server,
  },
  {
    title: "Billing",
    url: "/dashboard/billing",
    icon: CreditCard,
  },
  {
    title: "Cloud Providers",
    url: "/dashboard/providers",
    icon: Cloud,
  },
];

const secondaryMenuItems = [
  {
    title: "Settings",
    url: "/dashboard/settings",
    icon: Settings,
  },
  {
    title: "Get Help",
    url: "/dashboard/help",
    icon: HelpCircle,
  },
];

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
  const router = useRouter();
  const [user, setUser] = useState<any>(null);
  const [searchOpen, setSearchOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const userData = await api.auth.me();
        setUser(userData);
      } catch (error) {
        console.error('Failed to fetch user:', error);
      }
    };
    fetchUser();
  }, []);

  const fetchCsrf = async (): Promise<string | null> => {
    try {
      const res = await fetch("/api/csrf", { credentials: "include" });
      if (!res.ok) return null;
      const data = await res.json();
      return data?.token ?? null;
    } catch {
      return null;
    }
  };

  const handleLogout = async () => {
    try {
      const token = await fetchCsrf();
      const res = await fetch("/logout", {
        method: "POST",
        credentials: "include",
        headers: {
          ...(token ? { "X-XSRF-TOKEN": token } : {}),
        },
      });
      // Regardless of response, route to login
      router.push("/login");
    } catch (e) {
      router.push("/login");
    }
  };

  // TODO: Replace with actual notifications from backend
  const notifications = [
    { id: 1, title: "Resource limit approaching", time: "2 min ago", unread: true },
    { id: 2, title: "Backup completed successfully", time: "1 hour ago", unread: false },
    { id: 3, title: "New security alert", time: "3 hours ago", unread: true },
  ];

  const unreadCount = notifications.filter(n => n.unread).length;

  return (
    <SidebarProvider>
      <div className="flex min-h-screen w-full">
        <Sidebar className="hidden lg:flex">
          <SidebarHeader>
            <div className="flex items-center gap-2 px-4 py-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                <Cloud className="h-4 w-4" />
              </div>
              <span className="font-semibold">Cloud Mesh</span>
            </div>
          </SidebarHeader>
          <SidebarContent>
            {/* Primary Navigation */}
            <SidebarGroup>
              <SidebarGroupContent>
                <SidebarMenu>
                  {primaryMenuItems.map((item) => (
                    <SidebarMenuItem key={item.title}>
                      <SidebarMenuButton asChild>
                        <Link href={item.url}>
                          <item.icon className="h-4 w-4" />
                          <span>{item.title}</span>
                        </Link>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  ))}
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          </SidebarContent>
          
          {/* Secondary Navigation at Bottom */}
          <SidebarFooter>
            <SidebarGroup>
              <SidebarGroupContent>
                <SidebarMenu>
                  {secondaryMenuItems.map((item) => (
                    <SidebarMenuItem key={item.title}>
                      <SidebarMenuButton asChild>
                        <Link href={item.url}>
                          <item.icon className="h-4 w-4" />
                          <span>{item.title}</span>
                        </Link>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  ))}
                  
                  {/* Spacer between Get Help and Account Menu */}
                  <div className="py-2" />
                  
                  {/* Account Menu in Footer */}
                  <SidebarMenuItem>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <SidebarMenuButton className="w-full justify-start p-4 h-auto hover:bg-accent border border-border rounded-lg">
                          <div className="flex items-center gap-3 w-full">
                            <Avatar className="h-10 w-10">
                              <AvatarFallback className="bg-primary text-primary-foreground font-semibold">
                                {user?.username?.substring(0, 2).toUpperCase() || user?.email?.substring(0, 2).toUpperCase() || "U"}
                              </AvatarFallback>
                            </Avatar>
                            <div className="flex flex-col items-start overflow-hidden">
                              <span className="text-sm font-medium truncate">{user?.username || user?.email || "User"}</span>
                              <span className="text-xs text-muted-foreground truncate">{user?.email || "Loading..."}</span>
                            </div>
                          </div>
                        </SidebarMenuButton>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent className="w-64" align="start" side="right" sideOffset={8}>
                        <div className="flex items-center gap-3 p-3 border-b">
                          <Avatar className="h-8 w-8">
                            <AvatarFallback className="bg-primary text-primary-foreground font-semibold">
                              {user?.username?.substring(0, 2).toUpperCase() || user?.email?.substring(0, 2).toUpperCase() || "U"}
                            </AvatarFallback>
                          </Avatar>
                          <div className="flex flex-col">
                            <span className="font-medium">{user?.username || user?.email || "User"}</span>
                            <span className="text-sm text-muted-foreground">{user?.email || "Loading..."}</span>
                          </div>
                        </div>
                        <div className="py-2">
                          <DropdownMenuItem onClick={() => router.push('/dashboard/profile')} className="cursor-pointer">
                            <User className="mr-3 h-4 w-4" />
                            <span>View Profile</span>
                          </DropdownMenuItem>
                          <DropdownMenuItem onClick={() => router.push('/dashboard/billing')} className="cursor-pointer">
                            <CreditCard className="mr-3 h-4 w-4" />
                            <span>Billing</span>
                          </DropdownMenuItem>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-red-600 focus:text-red-600">
                            <LogOut className="mr-3 h-4 w-4" />
                            <span>Log out</span>
                          </DropdownMenuItem>
                        </div>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </SidebarMenuItem>
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          </SidebarFooter>
        </Sidebar>
        
        <div className="flex-1 flex flex-col">
          {/* Header */}
          <header className="flex h-16 items-center justify-between border-b px-4 lg:px-6">
            <div className="flex items-center gap-4">
              {/* Mobile Menu Toggle */}
              <Button
                variant="ghost"
                size="icon"
                className="lg:hidden"
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              >
                <Menu className="h-5 w-5" />
              </Button>
              
              {/* Desktop Sidebar Toggle */}
              <SidebarTrigger className="hidden lg:flex" />
              
              <h1 className="text-xl font-semibold hidden sm:block">Dashboard</h1>
            </div>
            
            <div className="flex items-center gap-2 lg:gap-4">
              {/* Search */}
              <Button
                variant="ghost"
                size="icon"
                onClick={() => setSearchOpen(true)}
                className="h-9 w-9"
              >
                <Search className="h-4 w-4" />
                <span className="sr-only">Search</span>
              </Button>
              
              {/* Theme Toggle */}
              <ThemeToggle />
              
              {/* Notifications */}
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="ghost" size="icon" className="relative h-9 w-9">
                    <Bell className="h-4 w-4" />
                    {unreadCount > 0 && (
                      <Badge 
                        variant="destructive" 
                        className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs"
                      >
                        {unreadCount}
                      </Badge>
                    )}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-80" align="end">
                  <div className="space-y-2">
                    <h3 className="font-medium text-sm">Notifications</h3>
                    <div className="space-y-2">
                      {notifications.map((notification) => (
                        <div
                          key={notification.id}
                          className={`p-2 rounded-md text-sm ${
                            notification.unread 
                              ? "bg-muted/50 border-l-2 border-primary" 
                              : "bg-muted/20"
                          }`}
                        >
                          <div className="font-medium">{notification.title}</div>
                          <div className="text-xs text-muted-foreground">{notification.time}</div>
                        </div>
                      ))}
                    </div>
                    <Button variant="ghost" className="w-full text-xs">
                      View all notifications
                    </Button>
                  </div>
                </PopoverContent>
              </Popover>
              
              {/* User Avatar */}
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                    <Avatar className="h-8 w-8">
                      <AvatarFallback>{user?.username?.substring(0, 2).toUpperCase() || user?.email?.substring(0, 2).toUpperCase() || "U"}</AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <DropdownMenuItem onClick={() => router.push('/dashboard/profile')}>
                    <User className="mr-2 h-4 w-4" />
                    <span>Profile</span>
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => router.push('/dashboard/settings')}>
                    <Settings className="mr-2 h-4 w-4" />
                    <span>Settings</span>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>Log out</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </header>
          
          {/* Mobile Navigation Overlay */}
          {mobileMenuOpen && (
            <div className="lg:hidden fixed inset-0 z-50 bg-background/80 backdrop-blur-sm">
              <div className="fixed left-0 top-0 h-full w-72 border-r bg-background p-6">
                <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                      <Cloud className="h-4 w-4" />
                    </div>
                    <span className="font-semibold">Cloud Mesh</span>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    ×
                  </Button>
                </div>
                
                <nav className="space-y-6">
                  <div className="space-y-2">
                    {primaryMenuItems.map((item) => (
                      <Link
                        key={item.title}
                        href={item.url}
                        className="flex items-center gap-3 px-3 py-2 rounded-md hover:bg-muted"
                        onClick={() => setMobileMenuOpen(false)}
                      >
                        <item.icon className="h-4 w-4" />
                        <span>{item.title}</span>
                      </Link>
                    ))}
                  </div>
                  
                  <div className="border-t pt-4 space-y-2">
                    {secondaryMenuItems.map((item) => (
                      <Link
                        key={item.title}
                        href={item.url}
                        className="flex items-center gap-3 px-3 py-2 rounded-md hover:bg-muted"
                        onClick={() => setMobileMenuOpen(false)}
                      >
                        <item.icon className="h-4 w-4" />
                        <span>{item.title}</span>
                      </Link>
                    ))}
                  </div>
                </nav>
              </div>
            </div>
          )}
          
          {/* Search Command Dialog */}
          <CommandDialog open={searchOpen} onOpenChange={setSearchOpen}>
            <CommandInput placeholder="Search resources, projects, settings..." />
            <CommandList>
              <CommandEmpty>No results found.</CommandEmpty>
              <CommandGroup heading="Quick Actions">
                <CommandItem>
                  <Server className="mr-2 h-4 w-4" />
                  <span>View all resources</span>
                </CommandItem>
                <CommandItem>
                  <FolderOpen className="mr-2 h-4 w-4" />
                  <span>Create new project</span>
                </CommandItem>
                <CommandItem>
                  <CreditCard className="mr-2 h-4 w-4" />
                  <span>View billing</span>
                </CommandItem>
              </CommandGroup>
              <CommandGroup heading="Navigation">
                {primaryMenuItems.map((item) => (
                  <CommandItem 
                    key={item.title}
                    onSelect={() => {
                      router.push(item.url);
                      setSearchOpen(false);
                    }}
                  >
                    <item.icon className="mr-2 h-4 w-4" />
                    <span>{item.title}</span>
                  </CommandItem>
                ))}
              </CommandGroup>
            </CommandList>
          </CommandDialog>
          
          {/* Main Content */}
          <main className="flex-1 overflow-y-auto p-6">
            {children}
          </main>
        </div>
      </div>
    </SidebarProvider>
  );
}
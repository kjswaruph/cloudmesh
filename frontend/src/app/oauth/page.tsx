// pages/oauth-redirect.tsx
import { useEffect } from 'react';
import { useRouter } from 'next/router';

export default function OAuthRedirect() {
    const router = useRouter();

    useEffect(() => {
        // Just redirect to dashboard
        router.push('/dashboard');
    }, [router]);

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto"></div>
                <p className="mt-4 text-gray-600">Completing authentication...</p>
            </div>
        </div>
    );
}
// 'use client';

// import { useState, useEffect } from 'react';
// import { RuntimeConfig } from '@/schemas/runtime-config';
// import { getRuntimeConfig } from '@/lib/config';

// /**
//  * Custom hook that manages the RuntimeConfig initialization
//  * @returns RuntimeConfig | null - Returns the RuntimeConfig or null while initializing
//  */
// export function useConfig() {
//   const [config, setConfig] = useState<RuntimeConfig | null>(null);

//   useEffect(() => {
//     getRuntimeConfig().then((config) => setConfig(config));
//   }, []);

//   return config;
// }

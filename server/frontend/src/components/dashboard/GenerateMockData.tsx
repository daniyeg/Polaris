import { UEData } from "./types";

export const generateMockData = (count = 50): UEData[] => {
  const gens = ['2G', '3G', '4G', '5G'];
  const techs = ['GSM', 'UMTS', 'LTE', 'NR'];
  const freqBands = ['B1', 'B3', 'B7', 'B8', 'B20', 'B38'];

  return Array.from({ length: count }, (_, i) => {
    const lat = 35.6892 + (Math.random() - 0.5) * 0.1;
    const lng = 51.3890 + (Math.random() - 0.5) * 0.1;
    const genIndex = Math.floor(Math.random() * gens.length);

    return {
      lat,
      lng,
      timestamp: Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000,
      gen: gens[genIndex],
      tech: techs[genIndex],
      plmn: [43211, 43235][Math.floor(Math.random() * 2)],
      lac: Math.floor(Math.random() * 10000),
      rac: Math.floor(Math.random() * 100),
      tac: Math.floor(Math.random() * 10000),
      freq_band: freqBands[Math.floor(Math.random() * freqBands.length)],
      afrn: Math.floor(Math.random() * 100),
      freq: Math.floor(Math.random() * 3000 + 700),
      rsrp: -Math.floor(Math.random() * 60 + 80),
      rsrq: -Math.floor(Math.random() * 15 + 5),
      ecno: Math.floor(Math.random() * 20 - 10),
      rxlev: Math.floor(Math.random() * 50 + 10)
    };
  });
}

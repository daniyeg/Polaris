export type UEData = {
  id: number;
  phone_number: string;
  lat: number;
  lng: number;
  timestamp: string;
  gen: string;
  tech: string;
  plmn: string;
  cid: number;
  lac: number;
  rac: number;
  tac: number;
  freq_band: string;
  afrn: number;
  freq: number;
  rsrp: number;
  rsrq: number;
  ecno: number;
  rxlev: number;
};

export type DashboardTabProps = {
  data: UEData[];
  isLoading?: boolean;
};

import { CityConfig, HazardWarning, MunicipalDispatch, Pothole, Severity, SEVERITY_INFO } from '../types/models';

export const CITY_CONFIGS: Record<string, CityConfig> = {
  Mangaluru: {
    cityName: 'Mangaluru',
    authorityName: 'Mangaluru City Corporation (MCC)',
    channelType: 'whatsapp',
    contactValue: '919449007722',
    instructions: 'Direct dispatch to MCC Official Grievance WhatsApp (+91 9449007722).'
  },
  Bengaluru: {
    cityName: 'Bengaluru',
    authorityName: 'Bruhat Bengaluru Mahanagara Palike (BBMP)',
    channelType: 'helpline',
    contactValue: '080-22660000',
    instructions: 'BBMP 24x7 Sahaaya Pothole Control Room (080-22660000).'
  },
  Udupi: {
    cityName: 'Udupi',
    authorityName: 'Udupi City Municipal Council (CMC)',
    channelType: 'helpline',
    contactValue: '0820-2520306',
    instructions: 'Udupi CMC Grievance Cell (0820-2520306).'
  },
  Mysuru: {
    cityName: 'Mysuru',
    authorityName: 'Mysuru City Corporation (MCC)',
    channelType: 'helpline',
    contactValue: '0821-2440890',
    instructions: 'Mysuru City Corporation Grievance Desk (0821-2440890).'
  }
};

const EARTH_RADIUS_M = 6371000;

export class GeoMatchingService {
  static haversineDistanceM(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const toRad = (deg: number) => (deg * Math.PI) / 180;
    const phi1 = toRad(lat1);
    const phi2 = toRad(lat2);
    const dPhi = toRad(lat2 - lat1);
    const dLambda = toRad(lon2 - lon1);

    const a =
      Math.sin(dPhi / 2) * Math.sin(dPhi / 2) +
      Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_M * c;
  }

  static bearingDeg(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const toRad = (deg: number) => (deg * Math.PI) / 180;
    const toDeg = (rad: number) => (rad * 180) / Math.PI;

    const phi1 = toRad(lat1);
    const phi2 = toRad(lat2);
    const dLambda = toRad(lon2 - lon1);

    const x = Math.sin(dLambda) * Math.cos(phi2);
    const y = Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(dLambda);
    const initialBearing = toDeg(Math.atan2(x, y));
    return (initialBearing + 360) % 360;
  }

  static angleDiffDeg(a: number, b: number): number {
    const diff = Math.abs(a - b) % 360;
    return diff <= 180 ? diff : 360 - diff;
  }

  static detectCity(lat: number, lon: number): string {
    if (lat >= 12.80 && lat <= 13.05 && lon >= 74.80 && lon <= 74.98) return 'Mangaluru';
    if (lat >= 12.80 && lat <= 13.20 && lon >= 77.40 && lon <= 77.85) return 'Bengaluru';
    if (lat >= 13.20 && lat <= 13.50 && lon >= 74.65 && lon <= 74.90) return 'Udupi';
    if (lat >= 12.20 && lat <= 12.45 && lon >= 76.50 && lon <= 76.80) return 'Mysuru';
    return 'Mangaluru';
  }

  static checkWarningAhead(
    currentLat: number,
    currentLon: number,
    headingDeg: number,
    potholes: Pothole[],
    alertRadiusM: number = 250,
    headingToleranceDeg: number = 45
  ): HazardWarning | null {
    const qualifying = potholes
      .filter(p => p.status === 'ACTIVE')
      .map(pothole => {
        const distance = this.haversineDistanceM(currentLat, currentLon, pothole.lat, pothole.lon);
        if (distance <= alertRadiusM) {
          const potholeBearing = this.bearingDeg(currentLat, currentLon, pothole.lat, pothole.lon);
          const deviation = this.angleDiffDeg(headingDeg, potholeBearing);
          if (deviation <= headingToleranceDeg) {
            return { distance, deviation, pothole };
          }
        }
        return null;
      })
      .filter((item): item is { distance: number; deviation: number; pothole: Pothole } => item !== null);

    if (qualifying.length === 0) return null;

    const nearest = qualifying.reduce((min, curr) => (curr.distance < min.distance ? curr : min), qualifying[0]);
    const isUrgent = nearest.pothole.severity === 'SEVERE' || nearest.distance < 90;

    return {
      potholeId: nearest.pothole.id,
      distanceMeters: Math.round(nearest.distance),
      angularDeviationDeg: nearest.deviation,
      severity: nearest.pothole.severity,
      address: nearest.pothole.address,
      confirmationCount: nearest.pothole.confirmationCount,
      message: `⚠️ ${nearest.pothole.severity} POTHOLE ${Math.round(nearest.distance)}m AHEAD`,
      isUrgent
    };
  }

  static buildComplaintText(
    address: string,
    lat: number,
    lon: number,
    severity: Severity,
    notes: string = ''
  ): string {
    const now = new Date();
    const timeStr = now.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' }) +
      ', ' + now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const mapsLink = `https://maps.google.com/?q=${lat.toFixed(5)},${lon.toFixed(5)}`;

    return `🚨 *ROAD POTHOLE HAZARD REPORT — RideVision*\n\n` +
      `📍 *Location:* ${address}\n` +
      `🌐 *Google Maps:* ${mapsLink}\n` +
      `⚠️ *Severity Assessment:* ${SEVERITY_INFO[severity].label.toUpperCase()}\n` +
      `🕒 *Reported At:* ${timeStr}\n` +
      `📝 *Commuter Note:* ${notes.trim() || 'Detected and verified via RideVision AI Road Safety System.'}\n\n` +
      `_Auto-formatted via RideVision CV Civic Dispatch._`;
  }

  static routeComplaint(
    city: string,
    address: string,
    lat: number,
    lon: number,
    severity: Severity,
    notes: string = ''
  ): MunicipalDispatch {
    const config = CITY_CONFIGS[city] || CITY_CONFIGS['Mangaluru'];
    const message = this.buildComplaintText(address, lat, lon, severity, notes);
    const encodedMsg = encodeURIComponent(message);

    let actionUrl = '';
    if (config.channelType === 'whatsapp') {
      const cleanNumber = config.contactValue.replace(/[^0-9]/g, '');
      actionUrl = `https://wa.me/${cleanNumber}?text=${encodedMsg}`;
    } else if (config.channelType === 'helpline') {
      actionUrl = `tel:${config.contactValue}`;
    } else {
      actionUrl = `mailto:${config.contactValue}?subject=${encodeURIComponent(`Road Pothole Hazard Report - ${city}`)}&body=${encodedMsg}`;
    }

    return {
      cityName: config.cityName,
      authorityName: config.authorityName,
      channelType: config.channelType,
      contactValue: config.contactValue,
      prefilledMessage: message,
      actionUrl,
      instructions: config.instructions
    };
  }
}

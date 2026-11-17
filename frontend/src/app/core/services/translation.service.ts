import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  currentLang = signal<'fr' | 'ar'>('fr');

  private fr: Record<string, string> = {
    app_title: "Groupe Chimique Tunisien",
    app_subtitle: "Portail de Gestion des Congés",
    nav_dashboard: "Tableau de bord",
    nav_conges: "Mes Congés",
    nav_demander: "Nouvelle Demande",
    nav_validations: "Demandes à viser / valider",
    nav_calendrier: "Planning / Calendrier",
    nav_employes: "Gestion Employés",
    nav_departements: "Départements",
    nav_logout: "Déconnexion",
    status_EN_ATTENTE: "En attente du chef",
    status_APPROUVE_CHEF: "Visé chef (attente RH)",
    status_APPROUVE: "Approuvé",
    status_REJETE: "Rejeté",
    status_ANNULE: "Annulé",
    rule_maternity: "Congé maternité validé automatiquement sans délai.",
    rule_dept_limit: "Règle GCT: Max 2 personnes simultanément en congé par département."
  };

  private ar: Record<string, string> = {
    app_title: "المجمع الكيميائي التونسي",
    app_subtitle: "بوابة التصرف في عطل الموظفين",
    nav_dashboard: "لوحة القيادة",
    nav_conges: "عطلي الشخصية",
    nav_demander: "طلب عطلة جديد",
    nav_validations: "المصادقة على العطل",
    nav_calendrier: "جدول العطل والغيابات",
    nav_employes: "إدارة الموظفين",
    nav_departements: "الإدارات والوحدات",
    nav_logout: "تسجيل الخروج",
    status_EN_ATTENTE: "في انتظار رئيس القسم",
    status_APPROUVE_CHEF: "موافقة رئيس القسم (في انتظار الموارد البشرية)",
    status_APPROUVE: "مقبول نهائياً",
    status_REJETE: "مرفوض",
    status_ANNULE: "ملغى",
    rule_maternity: "عطلة الأمومة تقبل آلياً وفورياً دون انتظار موافقة.",
    rule_dept_limit: "قاعدة المجمع: لا يمكن لأكثر من موظفين اثنين التمتع بعطلة في نفس الفترة بنفس القسم."
  };

  constructor() {
    const saved = localStorage.getItem('gct_lang') as 'fr' | 'ar';
    if (saved) {
      this.setLanguage(saved);
    }
  }

  setLanguage(lang: 'fr' | 'ar') {
    this.currentLang.set(lang);
    localStorage.setItem('gct_lang', lang);
    document.documentElement.lang = lang;
    document.body.setAttribute('dir', lang === 'ar' ? 'rtl' : 'ltr');
  }

  toggleLanguage() {
    this.setLanguage(this.currentLang() === 'fr' ? 'ar' : 'fr');
  }

  t(key: string): string {
    const dict = this.currentLang() === 'ar' ? this.ar : this.fr;
    return dict[key] || key;
  }
}

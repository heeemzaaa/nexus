import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API } from '../config/api';

type Target = 'USER' | 'PRODUCT' | 'UPDATE_PRODUCT'

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private http = inject(HttpClient)

  submitMedia(media: File[], target: Target, targetId: string) {
    const formData = new FormData();

    media.forEach(file => {
      formData.append('files', file);
    });

    formData.append('target', target);
    formData.append('targetId', targetId);

    return this.http.post<{ files: string[] }>(API.CREATE_MEDIA, formData);
  }
}

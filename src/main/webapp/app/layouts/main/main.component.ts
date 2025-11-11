import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { AccountService } from 'app/core/auth/account.service';
import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import FooterComponent from '../footer/footer.component';
import PageRibbonComponent from '../profiles/page-ribbon.component';

@Component({
  selector: 'jhi-main',
  templateUrl: './main.component.html',
  providers: [AppPageTitleStrategy],
  imports: [RouterOutlet, FooterComponent, PageRibbonComponent],
})
export default class MainComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly appPageTitleStrategy = inject(AppPageTitleStrategy);
  private readonly accountService = inject(AccountService);

  ngOnInit(): void {
    this.accountService.identity().subscribe({
      next: () => {
        // logged in, nothing else needed
      },
      error: err => {
        if (err.status === 401) {
          // guest user, ignore
          console.log('Guest user detected, skipping automatic login.');
        } else {
          console.error('Unexpected error fetching account:', err);
        }
      },
    });
  }
}

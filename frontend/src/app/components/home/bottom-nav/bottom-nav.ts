import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Icone } from '../../shared/icone/icone';

@Component({
  selector: 'app-bottom-nav',
  imports: [RouterLink, RouterLinkActive, Icone],
  templateUrl: './bottom-nav.html',
  styleUrl: './bottom-nav.scss'
})
export class BottomNav {}
